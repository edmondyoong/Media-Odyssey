package com.mo.mediaodyssey.community.favorite;

import com.mo.mediaodyssey.recommendation.UserInteractionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MediaRankingService.
 *
 * Covers:
 * - normalizeMediaType(): input normalisation (MOVIE / GAME / SONG)
 * - getTop10(): calls correct repository method and returns ranked list
 * - getTop10ByMediaType(): filters by category and normalises input
 * - enrichScoreRows(): fallback behaviour and display rating range
 */
@ExtendWith(MockitoExtension.class)
class MediaRankingServiceTest {

    @Mock
    private UserInteractionRepository userInteractionRepository;

    @InjectMocks
    private MediaRankingService rankingService;

    // ─── normalizeMediaType Tests ──────────────────────────────────────────────

    // "MOVIE", "movies", "Movies" should all map to "MOVIE"
    @Test
    void normalizeMediaType_movie_returnsNormalizedString() {
        assertThat(rankingService.normalizeMediaType("MOVIE")).isEqualTo("MOVIE");
        assertThat(rankingService.normalizeMediaType("movies")).isEqualTo("MOVIE");
        assertThat(rankingService.normalizeMediaType("Movies")).isEqualTo("MOVIE");
    }

    // "GAME" and "games" should both map to "GAME"
    @Test
    void normalizeMediaType_game_returnsNormalizedString() {
        assertThat(rankingService.normalizeMediaType("GAME")).isEqualTo("GAME");
        assertThat(rankingService.normalizeMediaType("games")).isEqualTo("GAME");
    }

    // "MUSIC", "song", "SONGS" should all map to "SONG" (internal canonical value)
    @Test
    void normalizeMediaType_music_returnsSong() {
        assertThat(rankingService.normalizeMediaType("MUSIC")).isEqualTo("SONG");
        assertThat(rankingService.normalizeMediaType("song")).isEqualTo("SONG");
        assertThat(rankingService.normalizeMediaType("SONGS")).isEqualTo("SONG");
    }

    // Null input should return empty string without throwing
    @Test
    void normalizeMediaType_null_returnsEmptyString() {
        assertThat(rankingService.normalizeMediaType(null)).isEqualTo("");
    }

    // Unknown input should be uppercased and returned as-is
    @Test
    void normalizeMediaType_unknown_returnsUppercase() {
        assertThat(rankingService.normalizeMediaType("podcast")).isEqualTo("PODCAST");
    }

    // ─── getTop10 Tests ────────────────────────────────────────────────────────

    // Empty DB → should return an empty list without errors
    @Test
    void getTop10_noInteractions_returnsEmptyList() {
        when(userInteractionRepository.findTop10ByScore())
                .thenReturn(Collections.emptyList());

        List<RankedMediaResponse> result = rankingService.getTop10();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    // getTop10() must call findTop10ByScore(), not the category-filtered version
    @Test
    void getTop10_callsCorrectRepositoryMethod() {
        when(userInteractionRepository.findTop10ByScore())
                .thenReturn(Collections.emptyList());

        rankingService.getTop10();

        verify(userInteractionRepository, times(1)).findTop10ByScore();
        verify(userInteractionRepository, never()).findTop10ByScoreAndMediaType(any());
    }

    // Result list should never exceed 10 items (enforced by the DB query)
    @Test
    void getTop10_returnsAtMost10Items() {
        List<Object[]> fakeRows = Arrays.asList(
                new Object[]{"11", "MOVIE", 130L},
                new Object[]{"22", "GAME",  95L},
                new Object[]{"33", "SONG",  50L}
        );
        when(userInteractionRepository.findTop10ByScore()).thenReturn(fakeRows);

        List<RankedMediaResponse> result = rankingService.getTop10();

        assertThat(result.size()).isLessThanOrEqualTo(10);
    }

    // ─── getTop10ByMediaType Tests ─────────────────────────────────────────────

    // "movies" input should be normalised to "MOVIE" before hitting the repository
    @Test
    void getTop10ByMediaType_movie_callsRepositoryWithNormalizedType() {
        when(userInteractionRepository.findTop10ByScoreAndMediaType("MOVIE"))
                .thenReturn(Collections.emptyList());

        rankingService.getTop10ByMediaType("movies");

        verify(userInteractionRepository, times(1))
                .findTop10ByScoreAndMediaType("MOVIE");
    }

    // "MUSIC" should be normalised to "SONG" (the DB canonical value)
    @Test
    void getTop10ByMediaType_music_normalizesToSong() {
        when(userInteractionRepository.findTop10ByScoreAndMediaType("SONG"))
                .thenReturn(Collections.emptyList());

        rankingService.getTop10ByMediaType("MUSIC");

        verify(userInteractionRepository, times(1))
                .findTop10ByScoreAndMediaType("SONG");
    }

    // Unknown category → repository returns nothing → result should be empty
    @Test
    void getTop10ByMediaType_unknownCategory_returnsEmptyList() {
        when(userInteractionRepository.findTop10ByScoreAndMediaType("PODCAST"))
                .thenReturn(Collections.emptyList());

        List<RankedMediaResponse> result = rankingService.getTop10ByMediaType("PODCAST");

        assertThat(result).isEmpty();
    }

    // ─── enrichScoreRows / RankedMediaResponse structure Tests ────────────────

    // Rows with an unrecognised mediaType should fall back to "Unknown" title (no crash)
    @Test
    void getTop10_rowWithUnknownMediaType_stillReturnsResult() {
        // explicitly typed to avoid List<Object> inference issue
        Object[] row = new Object[]{"999", "UNKNOWN", 20L};
        List<Object[]> fakeRows = Collections.singletonList(row);
        when(userInteractionRepository.findTop10ByScore()).thenReturn(fakeRows);

        List<RankedMediaResponse> result = rankingService.getTop10();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Unknown");
    }

    // Display rating derived from score must always be within the 1.0–5.0 star range
    @Test
    void getTop10_displayRating_isWithinValidRange() {
        Object[] row1 = new Object[]{"11", "UNKNOWN", 0L};
        Object[] row2 = new Object[]{"22", "UNKNOWN", 50L};
        Object[] row3 = new Object[]{"33", "UNKNOWN", 200L};
        List<Object[]> fakeRows = Arrays.asList(row1, row2, row3);
        when(userInteractionRepository.findTop10ByScore()).thenReturn(fakeRows);

        List<RankedMediaResponse> result = rankingService.getTop10();

        for (RankedMediaResponse item : result) {
            assertThat(item.getDisplayRating())
                    .isGreaterThanOrEqualTo(1.0)
                    .isLessThanOrEqualTo(5.0);
        }
    }
}