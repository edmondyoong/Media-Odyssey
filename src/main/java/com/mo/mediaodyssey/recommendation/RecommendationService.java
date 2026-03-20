package com.mo.mediaodyssey.recommendation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RecommendationService {

    private final UserInteractionRepository userInteractionRepository;
    private final BannedMediaRepository bannedMediaRepository;
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

    // maps genre names to TMDB genre IDs
    private static final Map<String, Integer> TMDB_GENRE_IDS = new HashMap<>();
    static {
        TMDB_GENRE_IDS.put("Action", 28);
        TMDB_GENRE_IDS.put("Adventure", 12);
        TMDB_GENRE_IDS.put("Animation", 16);
        TMDB_GENRE_IDS.put("Comedy", 35);
        TMDB_GENRE_IDS.put("Crime", 80);
        TMDB_GENRE_IDS.put("Documentary", 99);
        TMDB_GENRE_IDS.put("Drama", 18);
        TMDB_GENRE_IDS.put("Fantasy", 14);
        TMDB_GENRE_IDS.put("Horror", 27);
        TMDB_GENRE_IDS.put("Mystery", 9648);
        TMDB_GENRE_IDS.put("Romance", 10749);
        TMDB_GENRE_IDS.put("Science Fiction", 878);
        TMDB_GENRE_IDS.put("Thriller", 53);
    }

    public RecommendationService(UserInteractionRepository userInteractionRepository,
                                  BannedMediaRepository bannedMediaRepository) {
        this.userInteractionRepository = userInteractionRepository;
        this.bannedMediaRepository = bannedMediaRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // saves a user interaction — userId comes from the session, not the frontend
    public void recordInteraction(Long userId, InteractionRequest request) {
        UserInteraction interaction = new UserInteraction();
        interaction.setUserId(userId);
        interaction.setMediaApiId(request.getMediaApiId());
        interaction.setInteractionType(request.getInteractionType());
        interaction.setMediaType(request.getMediaType());
        interaction.setGenres(request.getGenres());
        interaction.setTimestamp(LocalDateTime.now());
        userInteractionRepository.save(interaction);
    }

    // returns the genre the user has engaged with most for a given media type
    // VIEW = 1 point, LIKE = 10 points
    private String userFavoriteGenre(Long userId, String mediaType) {
        List<UserInteraction> allUserInteractions = userInteractionRepository.findByUserId(userId);

        Map<String, Integer> genreScores = new HashMap<>();

        for (UserInteraction interaction : allUserInteractions) {
            if (interaction.getMediaType().equals(mediaType)) {

                int points = 0;
                switch (interaction.getInteractionType()) {
                    case "VIEW": points = 1; break;
                    case "LIKE": points = 10; break;
                    default: points = 0; break;
                }

                for (String genre : interaction.getGenres()) {
                    genreScores.put(genre, genreScores.getOrDefault(genre, 0) + points);
                }
            }
        }

        String favoriteGenre = null;
        int highestScore = 0;
        for (Map.Entry<String, Integer> entry : genreScores.entrySet()) {
            if (entry.getValue() > highestScore) {
                highestScore = entry.getValue();
                favoriteGenre = entry.getKey();
            }
        }

        return favoriteGenre;
    }

    // main recommendation method — finds user's favourite genre then calls the right API
    // falls back to popular/trending media for new users with no interaction history
    public List<RecommendationResponse> getRecommendations(Long userId, String mediaType) {
        String favoriteGenre = userFavoriteGenre(userId, mediaType);

        if (favoriteGenre == null) {
            switch (mediaType) {
                case "MOVIE": return fetchTmdbPopular();
                case "GAME":  return fetchRawgPopular();
                case "SONG":  return fetchSpotifyPopular();
                default:      return List.of();
            }
        }

        switch (mediaType) {
            case "MOVIE": return fetchTmdbRecommendations(favoriteGenre);
            case "GAME":  return fetchRawgRecommendations(favoriteGenre);
            case "SONG":  return fetchSpotifyRecommendations(favoriteGenre);
            default:      return List.of();
        }
    }

    // fallback: top popular movies from TMDB (no genre filter)
    private List<RecommendationResponse> fetchTmdbPopular() {
        List<RecommendationResponse> results = new ArrayList<>();
        String url = "https://api.themoviedb.org/3/movie/popular?api_key=" + tmdbApiKey;
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode movies = objectMapper.readTree(response).path("results");
            for (JsonNode movie : movies) {
                String mediaApiId = movie.path("id").asText();
                if (bannedMediaRepository.existsByMediaApiId(mediaApiId)) continue;
                String title    = movie.path("title").asText();
                String imageUrl = "https://image.tmdb.org/t/p/w500" + movie.path("poster_path").asText();
                double score    = movie.path("popularity").asDouble();
                results.add(new RecommendationResponse(mediaApiId, title, "", "MOVIE", "Popular", imageUrl, score));
            }
        } catch (Exception e) {
            System.err.println("TMDB popular fallback error: " + e.getMessage());
        }
        return results;
    }

    // fallback: top rated games from RAWG (no genre filter)
    private List<RecommendationResponse> fetchRawgPopular() {
        List<RecommendationResponse> results = new ArrayList<>();
        String url = "https://api.rawg.io/api/games?key=" + rawgApiKey + "&ordering=-rating&page_size=20";
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode games = objectMapper.readTree(response).path("results");
            for (JsonNode game : games) {
                String mediaApiId = game.path("id").asText();
                if (bannedMediaRepository.existsByMediaApiId(mediaApiId)) continue;
                String title    = game.path("name").asText();
                String imageUrl = game.path("background_image").asText();
                double score    = game.path("rating").asDouble();
                results.add(new RecommendationResponse(mediaApiId, title, "", "GAME", "Popular", imageUrl, score));
            }
        } catch (Exception e) {
            System.err.println("RAWG popular fallback error: " + e.getMessage());
        }
        return results;
    }

    // fallback: popular tracks from Spotify search
    private List<RecommendationResponse> fetchSpotifyPopular() {
        List<RecommendationResponse> results = new ArrayList<>();
        try {
            String accessToken = getSpotifyAccessToken();
            if (accessToken == null) return results;
            String url = "https://api.spotify.com/v1/search?q=you&type=track&limit=10&market=US";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            JsonNode tracks = objectMapper.readTree(response.getBody()).path("tracks").path("items");
            for (JsonNode track : tracks) {
                String mediaApiId = track.path("id").asText();
                if (bannedMediaRepository.existsByMediaApiId(mediaApiId)) continue;
                String title    = track.path("name").asText();
                String artist   = track.path("artists").path(0).path("name").asText();
                String imageUrl = track.path("album").path("images").path(0).path("url").asText();
                double score    = track.path("popularity").asDouble();
                results.add(new RecommendationResponse(mediaApiId, title, artist, "SONG", "Popular", imageUrl, score));
            }
        } catch (Exception e) {
            System.err.println("Spotify popular fallback error: " + e.getMessage());
        }
        return results;
    }

    // calls TMDB to get top movies in the user's favourite genre
    private List<RecommendationResponse> fetchTmdbRecommendations(String genre) {
        List<RecommendationResponse> results = new ArrayList<>();

        Integer genreId = TMDB_GENRE_IDS.get(genre);
        if (genreId == null) return results;

        String url = "https://api.themoviedb.org/3/discover/movie"
                + "?api_key=" + tmdbApiKey
                + "&with_genres=" + genreId
                + "&sort_by=popularity.desc";

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode movies = objectMapper.readTree(response).path("results");

            for (JsonNode movie : movies) {
                String mediaApiId = movie.path("id").asText();
                if (bannedMediaRepository.existsByMediaApiId(mediaApiId)) continue;
                String title    = movie.path("title").asText();
                String imageUrl = "https://image.tmdb.org/t/p/w500" + movie.path("poster_path").asText();
                double score    = movie.path("popularity").asDouble();
                results.add(new RecommendationResponse(mediaApiId, title, "", "MOVIE", genre, imageUrl, score));
            }
        } catch (Exception e) {
            System.err.println("TMDB API error: " + e.getMessage());
        }

        return results;
    }

    // calls RAWG to get top games in the user's favourite genre
    private List<RecommendationResponse> fetchRawgRecommendations(String genre) {
        List<RecommendationResponse> results = new ArrayList<>();

        String genreSlug = genre.toLowerCase().replace(" ", "-");

        String url = "https://api.rawg.io/api/games"
                + "?key=" + rawgApiKey
                + "&genres=" + genreSlug
                + "&ordering=-rating";

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode games = objectMapper.readTree(response).path("results");

            for (JsonNode game : games) {
                String mediaApiId = game.path("id").asText();
                if (bannedMediaRepository.existsByMediaApiId(mediaApiId)) continue;
                String title    = game.path("name").asText();
                String imageUrl = game.path("background_image").asText();
                double score    = game.path("rating").asDouble();
                results.add(new RecommendationResponse(mediaApiId, title, "", "GAME", genre, imageUrl, score));
            }
        } catch (Exception e) {
            System.err.println("RAWG API error: " + e.getMessage());
        }

        return results;
    }

    // calls Spotify search to get tracks for the user's favourite genre
    private List<RecommendationResponse> fetchSpotifyRecommendations(String genre) {
        List<RecommendationResponse> results = new ArrayList<>();

        try {
            String accessToken = getSpotifyAccessToken();
            if (accessToken == null) return results;

            String genreSlug = genre.toLowerCase().replace(" ", "-");
            String query = java.net.URLEncoder.encode("genre:" + genreSlug, "UTF-8");
            String url = "https://api.spotify.com/v1/search?q=" + query + "&type=track&limit=10&market=US";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode tracks = objectMapper.readTree(response.getBody()).path("tracks").path("items");

            for (JsonNode track : tracks) {
                String mediaApiId = track.path("id").asText();
                if (bannedMediaRepository.existsByMediaApiId(mediaApiId)) continue;
                String title    = track.path("name").asText();
                String artist   = track.path("artists").path(0).path("name").asText();
                String imageUrl = track.path("album").path("images").path(0).path("url").asText();
                double score    = track.path("popularity").asDouble();
                results.add(new RecommendationResponse(mediaApiId, title, artist, "SONG", genre, imageUrl, score));
            }
        } catch (Exception e) {
            System.err.println("Spotify API error: " + e.getMessage());
        }

        return results;
    }

    // Spotify requires an OAuth access token — this gets one using client credentials
    private String getSpotifyAccessToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(spotifyClientId, spotifyClientSecret);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://accounts.spotify.com/api/token", request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("access_token").asText();
        } catch (Exception e) {
            System.err.println("Spotify token error: " + e.getMessage());
            return null;
        }
    }

    // admin: ban a media item so it never appears in recommendations
    public void banMedia(String mediaApiId, String mediaType) {
        if (!bannedMediaRepository.existsByMediaApiId(mediaApiId)) {
            BannedMedia banned = new BannedMedia();
            banned.setMediaApiId(mediaApiId);
            banned.setMediaType(mediaType);
            bannedMediaRepository.save(banned);
        }
    }

    // admin: unban a media item
    @Transactional
    public void unbanMedia(String mediaApiId) {
        bannedMediaRepository.deleteByMediaApiId(mediaApiId);
    }
}