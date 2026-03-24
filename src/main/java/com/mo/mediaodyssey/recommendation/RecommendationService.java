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

    @Value("${lastfm.api.key}")
    private String lastfmApiKey;

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

    // reverse map: TMDB genre ID -> genre name
    private static final Map<Integer, String> TMDB_GENRE_NAMES = new HashMap<>();
    static {
        TMDB_GENRE_IDS.forEach((name, id) -> TMDB_GENRE_NAMES.put(id, name));
    }

    // genre tags used for Last.fm tag.getTopTracks — must be valid Last.fm tags
    private static final List<String> SPOTIFY_GENRES = Arrays.asList(
        "Pop", "Rock", "Hip-hop", "R&b", "Jazz", "Classical", "Electronic", "Country", "Reggae", "Soul"
    );

    // genre slugs used for RAWG — must match RAWG's accepted genre slugs
    private static final List<String> RAWG_GENRES = Arrays.asList(
        "Action", "Adventure", "Puzzle", "Strategy", "Shooter", "Racing", "Sports", "Simulation", "Indie", "Fighting"
    );

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

    // picks a random genre from the provided list, excluding the user's favourite
    private String pickOtherGenre(String favoriteGenre, List<String> genreList) {
        List<String> others = new ArrayList<>(genreList);
        others.removeIf(g -> g.equalsIgnoreCase(favoriteGenre));
        if (others.isEmpty()) return genreList.get(0);
        Collections.shuffle(others);
        return others.get(0);
    }

    // main recommendation method
    // returns 20 from the user's favourite genre + 10 from a random other genre
    // falls back to popular media for new users with no interaction history
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

        List<RecommendationResponse> results = new ArrayList<>();

        switch (mediaType) {
            case "MOVIE": {
                // 20 from favourite genre (TMDB always returns 20 per page)
                results.addAll(fetchTmdbRecommendations(favoriteGenre));
                // 10 from a random other TMDB genre
                String otherGenre = pickOtherGenre(favoriteGenre, new ArrayList<>(TMDB_GENRE_IDS.keySet()));
                List<RecommendationResponse> other = fetchTmdbRecommendations(otherGenre);
                results.addAll(other.subList(0, Math.min(10, other.size())));
                break;
            }
            case "GAME": {
                // 20 from favourite genre
                results.addAll(fetchRawgRecommendations(favoriteGenre, 20));
                // 10 from a random other RAWG genre
                String otherGenre = pickOtherGenre(favoriteGenre, RAWG_GENRES);
                results.addAll(fetchRawgRecommendations(otherGenre, 10));
                break;
            }
            case "SONG": {
                // 20 from favourite genre via Last.fm + Spotify search
                results.addAll(fetchSpotifyRecommendations(favoriteGenre, 20));
                // 10 from a random other genre via Last.fm + Spotify search
                String otherGenre = pickOtherGenre(favoriteGenre, SPOTIFY_GENRES);
                results.addAll(fetchSpotifyRecommendations(otherGenre, 10));
                break;
            }
            default:
                break;
        }

        return results;
    }

    // fallback: top popular movies from TMDB
    // extracts real genre from genre_ids array using the reverse TMDB_GENRE_NAMES map
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

                // extract first genre from genre_ids, fall back to "Action" if not found in our map
                String genre = "Action";
                JsonNode genreIds = movie.path("genre_ids");
                if (genreIds.isArray() && genreIds.size() > 0) {
                    int firstGenreId = genreIds.get(0).asInt();
                    genre = TMDB_GENRE_NAMES.getOrDefault(firstGenreId, "Action");
                }

                results.add(new RecommendationResponse(mediaApiId, title, "", "MOVIE", genre, imageUrl, score));
            }
        } catch (Exception e) {
            System.err.println("TMDB popular fallback error: " + e.getMessage());
        }
        return results;
    }

    // fallback: top rated games from RAWG
    // extracts real genre from the genres array returned by the RAWG API
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

                // extract first genre name from genres array, capitalise first letter
                String genre = "Action";
                JsonNode genres = game.path("genres");
                if (genres.isArray() && genres.size() > 0) {
                    String rawGenre = genres.get(0).path("name").asText();
                    if (!rawGenre.isEmpty()) {
                        genre = rawGenre.substring(0, 1).toUpperCase() + rawGenre.substring(1);
                    }
                }

                results.add(new RecommendationResponse(mediaApiId, title, "", "GAME", genre, imageUrl, score));
            }
        } catch (Exception e) {
            System.err.println("RAWG popular fallback error: " + e.getMessage());
        }
        return results;
    }

    // fallback: globally popular tracks for new users with no history
    // uses Last.fm chart.getTopTracks to get track names, then resolves each to a Spotify track
    private List<RecommendationResponse> fetchSpotifyPopular() {
        List<RecommendationResponse> results = new ArrayList<>();
        try {
            String accessToken = getSpotifyAccessToken();
            if (accessToken == null) return results;

            // Last.fm chart.getTopTracks returns the most popular tracks globally
            String lastfmUrl = "https://ws.audioscrobbler.com/2.0/?method=chart.getTopTracks"
                    + "&limit=20"
                    + "&api_key=" + lastfmApiKey
                    + "&format=json";

            String lastfmResponse = restTemplate.getForObject(lastfmUrl, String.class);
            JsonNode tracks = objectMapper.readTree(lastfmResponse).path("tracks").path("track");

            for (JsonNode track : tracks) {
                String trackName  = track.path("name").asText();
                String artistName = track.path("artist").path("name").asText();

                if (trackName.isEmpty() || artistName.isEmpty()) continue;

                // resolve to Spotify track to get ID and image
                RecommendationResponse rec = searchSpotifyTrack(trackName, artistName, "Pop", accessToken);
                if (rec != null && !bannedMediaRepository.existsByMediaApiId(rec.getMediaApiId())) {
                    results.add(rec);
                }
            }
        } catch (Exception e) {
            System.err.println("Spotify popular fallback error: " + e.getMessage());
        }
        return results;
    }

    // calls TMDB to get top movies in the user's favourite genre
    // TMDB always returns 20 results per page — page size cannot be changed
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
    // limit parameter controls how many results to return (RAWG supports page_size up to 40)
    private List<RecommendationResponse> fetchRawgRecommendations(String genre, int limit) {
        List<RecommendationResponse> results = new ArrayList<>();

        String genreSlug = genre.toLowerCase().replace(" ", "-");

        String url = "https://api.rawg.io/api/games"
                + "?key=" + rawgApiKey
                + "&genres=" + genreSlug
                + "&ordering=-rating"
                + "&page_size=" + limit;

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

    // uses Last.fm tag.getTopTracks to get top tracks for a genre tag,
    // then resolves each to a Spotify track via search to get the Spotify ID and image
    private List<RecommendationResponse> fetchSpotifyRecommendations(String genre, int limit) {
        List<RecommendationResponse> results = new ArrayList<>();

        try {
            String accessToken = getSpotifyAccessToken();
            if (accessToken == null) return results;

            // Last.fm tag.getTopTracks returns top tracks tagged with the given genre
            String tag = java.net.URLEncoder.encode(genre.toLowerCase(), "UTF-8");
            String lastfmUrl = "https://ws.audioscrobbler.com/2.0/?method=tag.getTopTracks"
                    + "&tag=" + tag
                    + "&limit=" + limit
                    + "&api_key=" + lastfmApiKey
                    + "&format=json";

            String lastfmResponse = restTemplate.getForObject(lastfmUrl, String.class);
            JsonNode tracks = objectMapper.readTree(lastfmResponse).path("tracks").path("track");

            for (JsonNode track : tracks) {
                String trackName  = track.path("name").asText();
                String artistName = track.path("artist").path("name").asText();

                if (trackName.isEmpty() || artistName.isEmpty()) continue;

                // resolve to Spotify track to get ID and image
                RecommendationResponse rec = searchSpotifyTrack(trackName, artistName, genre, accessToken);
                if (rec != null && !bannedMediaRepository.existsByMediaApiId(rec.getMediaApiId())) {
                    results.add(rec);
                }
            }
        } catch (Exception e) {
            System.err.println("Spotify recommendations error: " + e.getMessage());
        }

        return results;
    }

    // searches Spotify for a specific track by name and artist
    // returns a RecommendationResponse with the Spotify track ID and album image, or null if not found
    private RecommendationResponse searchSpotifyTrack(String trackName, String artistName, String genre, String accessToken) {
        try {
            String query = java.net.URLEncoder.encode(trackName + " artist:" + artistName, "UTF-8");
            String url = "https://api.spotify.com/v1/search?q=" + query + "&type=track&limit=1&market=US";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            JsonNode items = objectMapper.readTree(response.getBody()).path("tracks").path("items");
            if (!items.isArray() || items.size() == 0) return null;

            JsonNode track     = items.get(0);
            String mediaApiId  = track.path("id").asText();
            String title       = track.path("name").asText();
            String artist      = track.path("artists").path(0).path("name").asText();
            String imageUrl    = track.path("album").path("images").path(0).path("url").asText();
            double score       = track.path("popularity").asDouble();

            if (mediaApiId.isEmpty()) return null;

            return new RecommendationResponse(mediaApiId, title, artist, "SONG", genre, imageUrl, score);
        } catch (Exception e) {
            System.err.println("Spotify track search error: " + e.getMessage());
            return null;
        }
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