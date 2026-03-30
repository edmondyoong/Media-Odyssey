package com.mo.mediaodyssey.community.favorite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mo.mediaodyssey.recommendation.UserInteractionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for building the Community Favourites page data.
 *
 * Current iteration scope:
 * 1. Top 10 by internal score
 * 2. Metadata enrichment from external APIs
 * 3. UI-only star rating derived from score
 *
 * Deferred to later iteration:
 * - Fast-Rising section
 * - user-submitted star ratings
 * - richer normalization / weighting between categories
 */
@Service
public class MediaRankingService {

    private final UserInteractionRepository userInteractionRepository;
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

    public MediaRankingService(UserInteractionRepository userInteractionRepository) {
        this.userInteractionRepository = userInteractionRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Returns Top 10 ranked media across all categories.
     */
    public List<RankedMediaResponse> getTop10() {
        List<Object[]> rows = userInteractionRepository.findTop10ByScore();
        return enrichScoreRows(rows);
    }

    /**
     * Returns Top 10 ranked media for a specific category.
     */
    public List<RankedMediaResponse> getTop10ByMediaType(String mediaType) {
        List<Object[]> rows = userInteractionRepository.findTop10ByScoreAndMediaType(normalizeMediaType(mediaType));
        return enrichScoreRows(rows);
    }

    /**
     * Returns Top 5 Fast-Rising items: most LIKE interactions in the past 7
     * days
     * Each result is enriched with metadata from external APIs.
     * 
     * Row format from findTop5TrendingLikesSince:
     * [0] mediaApiId (String)
     * [1] mediaType (String)
     * [2] likeCount (Long)
     */
    public List<RankedMediaResponse> getFastRising5() {
        java.time.LocalDateTime since = java.time.LocalDateTime.now().minusDays(7);
        List<Object[]> rows = userInteractionRepository.findTop5TrendingLikesSince(since);

        List<RankedMediaResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            String mediaApiId = String.valueOf(row[0]);
            String mediaType = normalizeMediaType(String.valueOf(row[1]));
            long weeklyLikes = ((Number) row[2]).longValue();

            RankedMediaResponse enriched = fetchMetadata(mediaApiId, mediaType, weeklyLikes);
            if (enriched != null) {
                result.add(new RankedMediaResponse(enriched.getMediaApiId(), enriched.getTitle(), enriched.getArtist(), enriched.getMediaType(), enriched.getImageUrl(), enriched.getTotalScore(), enriched.getDisplayRating(), weeklyLikes));
            } else {
                result.add(new RankedMediaResponse(mediaApiId, "Unknown", "", mediaType, "", 0L, 1.0,  weeklyLikes));
            }
        }
        return result;
    }

    /**
     * Converts aggregated score rows into DTOs enriched with metadata.
     *
     * Row format:
     * [0] mediaApiId
     * [1] mediaType
     * [2] totalScore
     */
    private List<RankedMediaResponse> enrichScoreRows(List<Object[]> rows) {
        List<RankedMediaResponse> result = new ArrayList<>();

        for (Object[] row : rows) {
            String mediaApiId = String.valueOf(row[0]);
            String mediaType = normalizeMediaType(String.valueOf(row[1]));
            long totalScore = ((Number) row[2]).longValue();

            RankedMediaResponse enriched = fetchMetadata(mediaApiId, mediaType, totalScore);
            if (enriched != null) {
                result.add(enriched);
            } else {
                result.add(new RankedMediaResponse(
                        mediaApiId,
                        "Unknown",
                        "",
                        mediaType,
                        "",
                        totalScore,
                        toDisplayRating(totalScore)));
            }
        }

        return result;
    }

    /**
     * Maps internal score to a 1.0–5.0 display rating.
     *
     * This rating is only for UI readability.
     * Internal sorting still uses totalScore.
     *
     * TODO (later iteration):
     * Revisit this mapping after enough real interaction data is collected.
     * We may want a smoother formula or category-normalized scaling.
     */
    private double toDisplayRating(long totalScore) {
        if (totalScore <= 0)
            return 1.0;
        if (totalScore <= 10)
            return 1.5;
        if (totalScore <= 20)
            return 2.0;
        if (totalScore <= 35)
            return 2.5;
        if (totalScore <= 50)
            return 3.0;
        if (totalScore <= 70)
            return 3.5;
        if (totalScore <= 95)
            return 4.0;
        if (totalScore <= 130)
            return 4.5;
        return 5.0;
    }

    private RankedMediaResponse fetchMetadata(String mediaApiId, String mediaType, long totalScore) {
        return switch (mediaType) {
            case "MOVIE" -> fetchTmdbMetadata(mediaApiId, totalScore);
            case "GAME" -> fetchRawgMetadata(mediaApiId, totalScore);
            case "SONG" -> fetchSpotifyMetadata(mediaApiId, totalScore);
            default -> null;
        };
    }

    /**
     * Fetches movie metadata from TMDB.
     */
    private RankedMediaResponse fetchTmdbMetadata(String mediaApiId, long totalScore) {
        try {
            String url = "https://api.themoviedb.org/3/movie/" + mediaApiId + "?api_key=" + tmdbApiKey;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode movie = objectMapper.readTree(response);

            String title = movie.path("title").asText("Unknown");
            String posterPath = movie.path("poster_path").asText("");
            String imageUrl = posterPath.isBlank() ? "" : "https://image.tmdb.org/t/p/w500" + posterPath;

            return new RankedMediaResponse(
                    mediaApiId,
                    title,
                    "",
                    "MOVIE",
                    imageUrl,
                    totalScore,
                    toDisplayRating(totalScore));
        } catch (Exception e) {
            System.err.println("TMDB metadata error for " + mediaApiId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Fetches game metadata from RAWG.
     */
    private RankedMediaResponse fetchRawgMetadata(String mediaApiId, long totalScore) {
        try {
            String url = "https://api.rawg.io/api/games/" + mediaApiId + "?key=" + rawgApiKey;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode game = objectMapper.readTree(response);

            String title = game.path("name").asText("Unknown");
            String imageUrl = game.path("background_image").asText("");

            return new RankedMediaResponse(
                    mediaApiId,
                    title,
                    "",
                    "GAME",
                    imageUrl,
                    totalScore,
                    toDisplayRating(totalScore));
        } catch (Exception e) {
            System.err.println("RAWG metadata error for " + mediaApiId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Fetches track metadata from Spotify.
     * mediaApiId is expected to be a Spotify track id.
     *
     * TODO (later iteration):
     * Cache successful song metadata locally so repeated ranking page loads
     * do not need to call Spotify every time.
     */
    private RankedMediaResponse fetchSpotifyMetadata(String mediaApiId, long totalScore) {
        try {
            String accessToken = getSpotifyAccessToken();
            if (accessToken == null || accessToken.isBlank())
                return null;

            String url = "https://api.spotify.com/v1/tracks/" + mediaApiId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);

            JsonNode track = objectMapper.readTree(response.getBody());

            String title = track.path("name").asText("Unknown");
            String artist = track.path("artists").path(0).path("name").asText("");
            String imageUrl = track.path("album").path("images").path(0).path("url").asText("");

            return new RankedMediaResponse(
                    mediaApiId,
                    title,
                    artist,
                    "SONG",
                    imageUrl,
                    totalScore,
                    toDisplayRating(totalScore));
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

            org.springframework.util.MultiValueMap<String, String> body = new org.springframework.util.LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");

            HttpEntity<org.springframework.util.MultiValueMap<String, String>> request = new HttpEntity<>(body,
                    headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://accounts.spotify.com/api/token",
                    request,
                    String.class);

            return objectMapper.readTree(response.getBody()).path("access_token").asText();
        } catch (Exception e) {
            System.err.println("Spotify token error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Normalizes category/mediaType values to one of:
     * MOVIE / GAME / SONG
     */
    public String normalizeMediaType(String value) {
        if (value == null)
            return "";
        String upper = value.trim().toUpperCase();

        return switch (upper) {
            case "MOVIE", "MOVIES" -> "MOVIE";
            case "GAME", "GAMES" -> "GAME";
            case "SONG", "SONGS", "MUSIC" -> "SONG";
            default -> upper;
        };
    }
}