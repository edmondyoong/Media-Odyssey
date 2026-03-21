package com.mo.mediaodyssey.community.favorite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mo.mediaodyssey.recommendation.UserInteractionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service responsible for building the Community Favourites ranking.
 *
 * Flow:
 *   1. Query UserInteraction table to get Top 10 mediaApiIds by Point System score
 *   2. For each mediaApiId, call the appropriate external API (TMDB / RAWG / Spotify)
 *      to fetch the title, image, and artist (for songs)
 *   3. Return a list of RankedMediaResponse objects for the Thymeleaf template
 *
 * Point System: LIKE = 10 points, VIEW = 1 point
 * Trending: Top 5 most-liked items in the past 7 days
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

    // ── Top 10 Ranking ────────────────────────────────────────────────────────

    /**
     * Returns the Top 10 ranked media items across all categories.
     * Aggregates UserInteraction scores, then enriches with API metadata.
     */
    public List<RankedMediaResponse> getTop10() {
        List<Object[]> rows = userInteractionRepository.findTop10ByScore();
        return enrichWithMetadata(rows);
    }

    /**
     * Returns the Top 10 ranked media items filtered by media type.
     * Used when the user clicks the Movies, Games, or Songs tab.
     *
     * @param mediaType "MOVIE", "GAME", or "SONG"
     */
    public List<RankedMediaResponse> getTop10ByMediaType(String mediaType) {
        List<Object[]> rows = userInteractionRepository.findTop10ByScoreAndMediaType(mediaType);
        return enrichWithMetadata(rows);
    }

    // ── Fast-Rising Top 5 Trending ────────────────────────────────────────────

    /**
     * Returns the Top 5 fastest-rising media items based on likes in the past 7 days.
     * Used for the Fast-Rising section on the Community Favourites page.
     */
    public List<RankedMediaResponse> getTop5Trending() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> rows = userInteractionRepository.findTop5TrendingLikesSince(oneWeekAgo);
        return enrichWithMetadata(rows);
    }

    // ── Metadata Enrichment ───────────────────────────────────────────────────

    /**
     * Takes raw rows from the UserInteraction aggregate query and enriches
     * each one with title, imageUrl, and artist by calling the external API.
     *
     * Each row is Object[]:
     *   [0] = mediaApiId (String)
     *   [1] = mediaType  (String)
     *   [2] = score      (Long)
     */
    private List<RankedMediaResponse> enrichWithMetadata(List<Object[]> rows) {
        List<RankedMediaResponse> results = new ArrayList<>();

        for (Object[] row : rows) {
            String mediaApiId = (String) row[0];
            String mediaType  = (String) row[1];
            long   score      = ((Number) row[2]).longValue();

            RankedMediaResponse enriched = fetchMetadata(mediaApiId, mediaType, score);
            if (enriched != null) {
                results.add(enriched);
            }
        }

        return results;
    }

    /**
     * Dispatches to the correct API based on mediaType.
     * Returns null if the API call fails.
     */
    private RankedMediaResponse fetchMetadata(String mediaApiId, String mediaType, long score) {
        switch (mediaType) {
            case "MOVIE": return fetchTmdbMetadata(mediaApiId, score);
            case "GAME":  return fetchRawgMetadata(mediaApiId, score);
            case "SONG":  return fetchSpotifyMetadata(mediaApiId, score);
            default:      return null;
        }
    }

    // ── TMDB (Movies) ─────────────────────────────────────────────────────────

    /**
     * Fetches movie title and poster image from TMDB by movie ID.
     */
    private RankedMediaResponse fetchTmdbMetadata(String mediaApiId, long score) {
        try {
            String url = "https://api.themoviedb.org/3/movie/" + mediaApiId + "?api_key=" + tmdbApiKey;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode movie = objectMapper.readTree(response);

            String title    = movie.path("title").asText("Unknown");
            String imageUrl = "https://image.tmdb.org/t/p/w500" + movie.path("poster_path").asText();

            return new RankedMediaResponse(mediaApiId, title, "", "MOVIE", imageUrl, score, 0);
        } catch (Exception e) {
            System.err.println("TMDB metadata error for " + mediaApiId + ": " + e.getMessage());
            return null;
        }
    }

    // ── RAWG (Games) ──────────────────────────────────────────────────────────

    /**
     * Fetches game title and background image from RAWG by game ID.
     */
    private RankedMediaResponse fetchRawgMetadata(String mediaApiId, long score) {
        try {
            String url = "https://api.rawg.io/api/games/" + mediaApiId + "?key=" + rawgApiKey;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode game = objectMapper.readTree(response);

            String title    = game.path("name").asText("Unknown");
            String imageUrl = game.path("background_image").asText("");

            return new RankedMediaResponse(mediaApiId, title, "", "GAME", imageUrl, score, 0);
        } catch (Exception e) {
            System.err.println("RAWG metadata error for " + mediaApiId + ": " + e.getMessage());
            return null;
        }
    }

    // ── Spotify (Songs) ───────────────────────────────────────────────────────

    /**
     * Fetches track title, artist name, and album art from Spotify by track ID.
     */
    private RankedMediaResponse fetchSpotifyMetadata(String mediaApiId, long score) {
        try {
            String accessToken = getSpotifyAccessToken();
            if (accessToken == null) return null;

            String url = "https://api.spotify.com/v1/tracks/" + mediaApiId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            JsonNode track = objectMapper.readTree(response.getBody());

            String title    = track.path("name").asText("Unknown");
            String artist   = track.path("artists").path(0).path("name").asText("");
            String imageUrl = track.path("album").path("images").path(0).path("url").asText("");

            return new RankedMediaResponse(mediaApiId, title, artist, "SONG", imageUrl, score, 0);
        } catch (Exception e) {
            System.err.println("Spotify metadata error for " + mediaApiId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtains a Spotify OAuth access token using the Client Credentials flow.
     * Required before making any Spotify API calls.
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
                    "https://accounts.spotify.com/api/token", request, String.class);

            return objectMapper.readTree(response.getBody()).path("access_token").asText();
        } catch (Exception e) {
            System.err.println("Spotify token error: " + e.getMessage());
            return null;
        }
    }
}