package com.mo.mediaodyssey.community.favorite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mo.mediaodyssey.recommendation.UserInteractionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for building the Community Favourites page data.
 *
 * Data sources:
 * 1. UserInteraction table
 *    - Top 10 by score
 *    - Top 5 trending this week
 *
 * 2. Media table
 *    - Top Rated ranking
 *    - Rating persistence (ratingSum / ratingCount)
 *
 * This service also enriches ranking rows with metadata from:
 * - TMDB (movies)
 * - RAWG (games)
 * - Spotify (songs)
 */
@Service
public class MediaRankingService {

    private final UserInteractionRepository userInteractionRepository;
    private final MediaRepository mediaRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    @Value("${rawg.api.key}")
    private String rawgApiKey;

    @Value("${spotify.client.id}")
    private String spotifyClientId;

    @Value("${spotify.client.secret}")
    private String spotifyClientSecret;

    public MediaRankingService(UserInteractionRepository userInteractionRepository,
                               MediaRepository mediaRepository) {
        this.userInteractionRepository = userInteractionRepository;
        this.mediaRepository = mediaRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // ─────────────────────────────────────────────────────────────
    // Top 10 by score
    // Source: UserInteraction aggregate query
    // ─────────────────────────────────────────────────────────────

    /**
     * Returns Top 10 ranked media across all categories using score.
     * score = views * 1 + likes * 10
     */
    public List<RankedMediaResponse> getTop10() {
        List<Object[]> rows = userInteractionRepository.findTop10ByScore();
        return enrichScoreRows(rows);
    }

    /**
     * Returns Top 10 ranked media for a specific media type using score.
     *
     * @param mediaType MOVIE / GAME / SONG
     */
    public List<RankedMediaResponse> getTop10ByMediaType(String mediaType) {
        List<Object[]> rows = userInteractionRepository.findTop10ByScoreAndMediaType(normalizeMediaType(mediaType));
        return enrichScoreRows(rows);
    }

    // ─────────────────────────────────────────────────────────────
    // Top 5 trending this week
    // Source: UserInteraction aggregate query
    // ─────────────────────────────────────────────────────────────

    /**
     * Returns Top 5 fastest-rising media based on likes in the past 7 days.
     *
     * Important:
     * The query result for trending uses weekly likes, not total score.
     * We preserve weeklyLikes so the Top 5 section shows the correct value.
     */
    public List<RankedMediaResponse> getTop5Trending() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> rows = userInteractionRepository.findTop5TrendingLikesSince(oneWeekAgo);
        return enrichTrendingRows(rows);
    }

    // ─────────────────────────────────────────────────────────────
    // Top Rated
    // Source: Media table (ratingSum / ratingCount)
    // ─────────────────────────────────────────────────────────────

    /**
     * Returns Top 10 by average rating across all categories.
     */
    public List<RankedMediaResponse> getTop10ByRating() {
        return mediaRepository.findTop10ByRating()
                .stream()
                .map(this::toRatedResponse)
                .toList();
    }

    /**
     * Returns Top 10 by average rating for a specific media type.
     *
     * @param mediaType MOVIE / GAME / SONG
     */
    public List<RankedMediaResponse> getTop10ByRatingAndMediaType(String mediaType) {
        return mediaRepository.findTop10ByRatingAndCategory(normalizeMediaType(mediaType))
                .stream()
                .map(this::toRatedResponse)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────
    // Rating persistence
    // Source: Media table
    // ─────────────────────────────────────────────────────────────

    /**
     * Adds a rating to the Media table.
     *
     * We identify the row by:
     * - externalApiId
     * - category
     *
     * If the media row does not exist yet, we create a placeholder entry
     * using metadata fetched from the external API.
     */
    @Transactional
    public void addRating(String mediaApiId, String mediaType, int stars) {
        String normalizedType = normalizeMediaType(mediaType);

        Media media = mediaRepository.findByExternalApiIdAndCategoryIgnoreCase(mediaApiId, normalizedType)
                .orElseGet(() -> createPlaceholderMedia(mediaApiId, normalizedType));

        media.setRatingSum(media.getRatingSum() + stars);
        media.setRatingCount(media.getRatingCount() + 1);

        mediaRepository.save(media);
    }

    /**
     * Creates a Media row if rating is submitted before the item has been cached.
     * This prevents the rate endpoint from failing when the DB row does not exist yet.
     */
    private Media createPlaceholderMedia(String mediaApiId, String mediaType) {
        RankedMediaResponse metadata = fetchMetadata(mediaApiId, mediaType, 0, 0);

        Media media = new Media();
        media.setExternalApiId(mediaApiId);
        media.setCategory(mediaType);

        if (metadata != null) {
            media.setTitle(metadata.getTitle());
            media.setArtist(metadata.getArtist());
            media.setImageUrl(metadata.getImageUrl());
        } else {
            media.setTitle("Unknown");
            media.setArtist("");
            media.setImageUrl("");
        }

        media.setViews(0);
        media.setLikes(0);
        media.setLikesLastWeek(0);
        media.setRatingSum(0);
        media.setRatingCount(0);

        return mediaRepository.save(media);
    }

    /**
     * Converts a Media entity into RankedMediaResponse for Top Rated UI.
     */
    private RankedMediaResponse toRatedResponse(Media media) {
        return new RankedMediaResponse(
                media.getExternalApiId() == null ? String.valueOf(media.getId()) : media.getExternalApiId(),
                media.getTitle(),
                media.getArtist(),
                normalizeMediaType(media.getCategory()),
                media.getImageUrl() == null ? "" : media.getImageUrl(),
                media.getTotalScore(),
                0,
                media.getAverageRating(),
                media.getRatingCount()
        );
    }

    // ─────────────────────────────────────────────────────────────
    // Metadata enrichment
    // ─────────────────────────────────────────────────────────────

    /**
     * Enriches Top 10 score rows with API metadata.
     *
     * Expected row structure:
     * [0] mediaApiId
     * [1] mediaType
     * [2] totalScore
     */
    private List<RankedMediaResponse> enrichScoreRows(List<Object[]> rows) {
        List<RankedMediaResponse> results = new ArrayList<>();

        for (Object[] row : rows) {
            String mediaApiId = String.valueOf(row[0]);
            String mediaType = normalizeMediaType(String.valueOf(row[1]));
            long score = ((Number) row[2]).longValue();

            RankedMediaResponse enriched = fetchMetadata(mediaApiId, mediaType, score, 0);
            if (enriched != null) {
                results.add(enriched);
                cacheMediaMetadata(enriched);
            }
        }

        return results;
    }

    /**
     * Enriches Top 5 trending rows with API metadata.
     *
     * Expected row structure:
     * [0] mediaApiId
     * [1] mediaType
     * [2] weeklyLikes
     *
     * This is separated from score enrichment because trending cards need
     * weeklyLikes to display correctly.
     */
    private List<RankedMediaResponse> enrichTrendingRows(List<Object[]> rows) {
        List<RankedMediaResponse> results = new ArrayList<>();

        for (Object[] row : rows) {
            String mediaApiId = String.valueOf(row[0]);
            String mediaType = normalizeMediaType(String.valueOf(row[1]));
            long weeklyLikes = ((Number) row[2]).longValue();

            RankedMediaResponse enriched = fetchMetadata(mediaApiId, mediaType, 0, weeklyLikes);
            if (enriched != null) {
                results.add(enriched);
                cacheMediaMetadata(enriched);
            }
        }

        return results;
    }

    /**
     * Stores basic metadata into the Media table.
     *
     * This helps keep title / artist / image cached locally, especially useful
     * when a user rates something before that item has been saved before.
     */
    @Transactional
    protected void cacheMediaMetadata(RankedMediaResponse response) {
        if (response == null || response.getMediaApiId() == null || response.getMediaType() == null) {
            return;
        }

        String mediaApiId = response.getMediaApiId();
        String mediaType = normalizeMediaType(response.getMediaType());

        Optional<Media> existingOpt = mediaRepository.findByExternalApiIdAndCategoryIgnoreCase(mediaApiId, mediaType);
        Media media = existingOpt.orElseGet(Media::new);

        if (media.getExternalApiId() == null) {
            media.setExternalApiId(mediaApiId);
            media.setCategory(mediaType);
        }

        media.setTitle(response.getTitle() == null || response.getTitle().isBlank() ? "Unknown" : response.getTitle());
        media.setArtist(response.getArtist() == null ? "" : response.getArtist());
        media.setImageUrl(response.getImageUrl() == null ? "" : response.getImageUrl());

        mediaRepository.save(media);
    }

    /**
     * Fetches metadata using the correct external API based on media type.
     */
    private RankedMediaResponse fetchMetadata(String mediaApiId, String mediaType, long totalScore, long weeklyLikes) {
        return switch (normalizeMediaType(mediaType)) {
            case "MOVIE" -> fetchTmdbMetadata(mediaApiId, totalScore, weeklyLikes);
            case "GAME" -> fetchRawgMetadata(mediaApiId, totalScore, weeklyLikes);
            case "SONG" -> fetchSpotifyMetadata(mediaApiId, totalScore, weeklyLikes);
            default -> null;
        };
    }

    // ─────────────────────────────────────────────────────────────
    // TMDB
    // ─────────────────────────────────────────────────────────────

    /**
     * Fetches movie metadata from TMDB.
     */
    private RankedMediaResponse fetchTmdbMetadata(String mediaApiId, long totalScore, long weeklyLikes) {
        try {
            String url = "https://api.themoviedb.org/3/movie/" + mediaApiId + "?api_key=" + tmdbApiKey;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode movie = objectMapper.readTree(response);

            String title = movie.path("title").asText("Unknown");
            String posterPath = movie.path("poster_path").asText("");
            String imageUrl = posterPath.isBlank() ? "" : "https://image.tmdb.org/t/p/w500" + posterPath;

            Media cached = mediaRepository.findByExternalApiIdAndCategoryIgnoreCase(mediaApiId, "MOVIE").orElse(null);
            double avgRating = cached == null ? 0.0 : cached.getAverageRating();
            int ratingCount = cached == null ? 0 : cached.getRatingCount();

            return new RankedMediaResponse(
                    mediaApiId,
                    title,
                    "",
                    "MOVIE",
                    imageUrl,
                    totalScore,
                    weeklyLikes,
                    avgRating,
                    ratingCount
            );
        } catch (Exception e) {
            System.err.println("TMDB metadata error for " + mediaApiId + ": " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // RAWG
    // ─────────────────────────────────────────────────────────────

    /**
     * Fetches game metadata from RAWG.
     */
    private RankedMediaResponse fetchRawgMetadata(String mediaApiId, long totalScore, long weeklyLikes) {
        try {
            String url = "https://api.rawg.io/api/games/" + mediaApiId + "?key=" + rawgApiKey;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode game = objectMapper.readTree(response);

            String title = game.path("name").asText("Unknown");
            String imageUrl = game.path("background_image").asText("");

            Media cached = mediaRepository.findByExternalApiIdAndCategoryIgnoreCase(mediaApiId, "GAME").orElse(null);
            double avgRating = cached == null ? 0.0 : cached.getAverageRating();
            int ratingCount = cached == null ? 0 : cached.getRatingCount();

            return new RankedMediaResponse(
                    mediaApiId,
                    title,
                    "",
                    "GAME",
                    imageUrl,
                    totalScore,
                    weeklyLikes,
                    avgRating,
                    ratingCount
            );
        } catch (Exception e) {
            System.err.println("RAWG metadata error for " + mediaApiId + ": " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Spotify
    // ─────────────────────────────────────────────────────────────

    /**
     * Fetches track metadata from Spotify.
     */
    private RankedMediaResponse fetchSpotifyMetadata(String mediaApiId, long totalScore, long weeklyLikes) {
        try {
            String accessToken = getSpotifyAccessToken();
            if (accessToken == null) return null;

            String url = "https://api.spotify.com/v1/tracks/" + mediaApiId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            JsonNode track = objectMapper.readTree(response.getBody());

            String title = track.path("name").asText("Unknown");
            String artist = track.path("artists").path(0).path("name").asText("");
            String imageUrl = track.path("album").path("images").path(0).path("url").asText("");

            Media cached = mediaRepository.findByExternalApiIdAndCategoryIgnoreCase(mediaApiId, "SONG").orElse(null);
            double avgRating = cached == null ? 0.0 : cached.getAverageRating();
            int ratingCount = cached == null ? 0 : cached.getRatingCount();

            return new RankedMediaResponse(
                    mediaApiId,
                    title,
                    artist,
                    "SONG",
                    imageUrl,
                    totalScore,
                    weeklyLikes,
                    avgRating,
                    ratingCount
            );
        } catch (Exception e) {
            System.err.println("Spotify metadata error for " + mediaApiId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Requests a Spotify access token using client credentials flow.
     */
    private String getSpotifyAccessToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(spotifyClientId, spotifyClientSecret);

            org.springframework.util.MultiValueMap<String, String> body =
                    new org.springframework.util.LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");

            HttpEntity<org.springframework.util.MultiValueMap<String, String>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://accounts.spotify.com/api/token",
                    request,
                    String.class
            );

            return objectMapper.readTree(response.getBody()).path("access_token").asText();
        } catch (Exception e) {
            System.err.println("Spotify token error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Normalizes category/mediaType values to one of:
     * MOVIE / GAME / SONG
     *
     * This helps when different parts of the app use slightly different names.
     */
    private String normalizeMediaType(String value) {
        if (value == null) return "";
        String upper = value.trim().toUpperCase();

        return switch (upper) {
            case "MOVIE", "MOVIES" -> "MOVIE";
            case "GAME", "GAMES" -> "GAME";
            case "SONG", "SONGS", "MUSIC" -> "SONG";
            default -> upper;
        };
    }
}