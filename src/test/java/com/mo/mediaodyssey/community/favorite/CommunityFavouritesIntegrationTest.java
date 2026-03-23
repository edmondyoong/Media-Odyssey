package com.mo.mediaodyssey.community.favorite;

import com.mo.mediaodyssey.recommendation.UserInteraction;
import com.mo.mediaodyssey.recommendation.UserInteractionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Community Favourites feature.
 *
 * These tests load the full Spring context and connect to the real PostgreSQL DB.
 * They will FAIL if:
 * - The DB connection is misconfigured or unreachable
 * - UserInteraction cannot be saved or queried
 * - The ranking query returns unexpected results
 * - External API keys (TMDB, RAWG, Spotify) are invalid or missing
 *
 * Run with: ./mvnw test
 * Requires: valid DB and API credentials in application.properties or .env
 */
@SpringBootTest
class CommunityFavouritesIntegrationTest {

    @Autowired
    private UserInteractionRepository userInteractionRepository;

    @Autowired
    private MediaRankingService mediaRankingService;

    // Test-only user IDs — cleaned up after each test
    private static final long TEST_USER_ID_BASE = 99990L;

    @AfterEach
    void cleanUp() {
        // Remove any interactions saved by these tests so they don't pollute the DB
        for (long uid = TEST_USER_ID_BASE; uid <= TEST_USER_ID_BASE + 9; uid++) {
            userInteractionRepository.deleteAll(
                userInteractionRepository.findByUserId(uid)
            );
        }
    }

    // ─── 1. DB Connection / Context Load ──────────────────────────────────────

    // Fails if Spring context cannot start (DB unreachable, misconfigured beans, etc.)
    @Test
    void contextLoads_dbConnectionSucceeds() {
        assertThat(userInteractionRepository).isNotNull();
        assertThat(mediaRankingService).isNotNull();
    }

    // ─── 2. UserInteraction Save / Query ──────────────────────────────────────

    // Saves a UserInteraction to the real DB and retrieves it —
    // fails if the entity mapping, DB schema, or repository is broken
    @Test
    void userInteraction_saveAndRetrieve_succeeds() {
        // Given: a new VIEW interaction
        UserInteraction interaction = new UserInteraction();
        interaction.setUserId(TEST_USER_ID_BASE);
        interaction.setMediaApiId("integration-test-media");
        interaction.setMediaType("MOVIE");
        interaction.setInteractionType("VIEW");
        interaction.setTimestamp(LocalDateTime.now());
        interaction.setGenres(List.of("Action"));

        // When: saved to DB
        UserInteraction saved = userInteractionRepository.save(interaction);

        // Then: DB assigned an ID and value can be retrieved
        assertThat(saved.getId()).isNotNull();

        List<UserInteraction> found = userInteractionRepository.findByUserId(TEST_USER_ID_BASE);
        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getMediaApiId()).isEqualTo("integration-test-media");
        assertThat(found.get(0).getInteractionType()).isEqualTo("VIEW");
    }

    // Saves VIEW + LIKE interactions and verifies the ranking query scores them correctly —
    // fails if the JPQL ranking query or score formula is broken (VIEW=1, LIKE=10)
    @Test
    void findTop10ByScore_viewAndLike_scoresCorrectly() {
        String testMediaId = "ranking-integration-" + System.currentTimeMillis();

        UserInteraction view = new UserInteraction();
        view.setUserId(TEST_USER_ID_BASE + 1);
        view.setMediaApiId(testMediaId);
        view.setMediaType("MOVIE");
        view.setInteractionType("VIEW");
        view.setTimestamp(LocalDateTime.now());
        view.setGenres(List.of());

        UserInteraction like = new UserInteraction();
        like.setUserId(TEST_USER_ID_BASE + 1);
        like.setMediaApiId(testMediaId);
        like.setMediaType("MOVIE");
        like.setInteractionType("LIKE");
        like.setTimestamp(LocalDateTime.now());
        like.setGenres(List.of());

        userInteractionRepository.save(view);
        userInteractionRepository.save(like);

        // When: ranking query runs
        List<Object[]> rows = userInteractionRepository.findTop10ByScore();

        // Then: our test media appears with score >= 11 (VIEW=1 + LIKE=10)
        boolean found = rows.stream().anyMatch(row -> {
            String mediaApiId = String.valueOf(row[0]);
            long score = ((Number) row[2]).longValue();
            return mediaApiId.equals(testMediaId) && score >= 11L;
        });

        assertThat(found)
            .as("Expected '%s' in ranking with score >= 11 (VIEW=1 + LIKE=10)", testMediaId)
            .isTrue();
    }

    // ─── 3. External API Calls ─────────────────────────────────────────────────

    // Calls the real TMDB API with a known movie ID (27205 = Inception) —
    // fails if the TMDB API key is missing, expired, or the response format changed
    @Test
    void tmdbApi_fetchKnownMovie_returnsValidTitle() {
        // Seed DB so getTop10ByMediaType("MOVIE") includes this item
        UserInteraction seed = new UserInteraction();
        seed.setUserId(TEST_USER_ID_BASE + 2);
        seed.setMediaApiId("27205");   // Inception on TMDB
        seed.setMediaType("MOVIE");
        seed.setInteractionType("VIEW");
        seed.setTimestamp(LocalDateTime.now());
        seed.setGenres(List.of());
        userInteractionRepository.save(seed);

        List<RankedMediaResponse> result = mediaRankingService.getTop10ByMediaType("MOVIE");

        // Fails if TMDB API key is invalid or "27205" no longer resolves to a title
        boolean hasTmdbTitle = result.stream()
            .anyMatch(r -> r.getMediaApiId().equals("27205")
                        && r.getTitle() != null
                        && !r.getTitle().isBlank()
                        && !r.getTitle().equals("Unknown"));

        assertThat(hasTmdbTitle)
            .as("TMDB API should return a valid title for movie id=27205 (Inception)")
            .isTrue();
    }

    // Calls the real RAWG API with a known game ID (3498 = GTA V) —
    // fails if the RAWG API key is missing or invalid
    @Test
    void rawgApi_fetchKnownGame_returnsValidTitle() {
        UserInteraction seed = new UserInteraction();
        seed.setUserId(TEST_USER_ID_BASE + 3);
        seed.setMediaApiId("3498");   // GTA V on RAWG
        seed.setMediaType("GAME");
        seed.setInteractionType("VIEW");
        seed.setTimestamp(LocalDateTime.now());
        seed.setGenres(List.of());
        userInteractionRepository.save(seed);

        List<RankedMediaResponse> result = mediaRankingService.getTop10ByMediaType("GAME");

        // Fails if RAWG API key is invalid or "3498" no longer resolves to a title
        boolean hasRawgTitle = result.stream()
            .anyMatch(r -> r.getMediaApiId().equals("3498")
                        && r.getTitle() != null
                        && !r.getTitle().isBlank()
                        && !r.getTitle().equals("Unknown"));

        assertThat(hasRawgTitle)
            .as("RAWG API should return a valid title for game id=3498 (GTA V)")
            .isTrue();
    }

    // Calls the real Spotify API with a known track ID (Bohemian Rhapsody) —
    // fails if Spotify client credentials are missing or the token request fails
    @Test
    void spotifyApi_fetchKnownTrack_returnsValidTitle() {
        String trackId = "7tFiyTwD0nx5a1eklYtX2J";   // Bohemian Rhapsody - Queen

        UserInteraction seed = new UserInteraction();
        seed.setUserId(TEST_USER_ID_BASE + 4);
        seed.setMediaApiId(trackId);
        seed.setMediaType("SONG");
        seed.setInteractionType("VIEW");
        seed.setTimestamp(LocalDateTime.now());
        seed.setGenres(List.of());
        userInteractionRepository.save(seed);

        List<RankedMediaResponse> result = mediaRankingService.getTop10ByMediaType("SONG");

        // Fails if Spotify credentials are invalid or track no longer exists
        boolean hasSpotifyTitle = result.stream()
            .anyMatch(r -> r.getMediaApiId().equals(trackId)
                        && r.getTitle() != null
                        && !r.getTitle().isBlank()
                        && !r.getTitle().equals("Unknown"));

        assertThat(hasSpotifyTitle)
            .as("Spotify API should return a valid title for track id=" + trackId)
            .isTrue();
    }
}