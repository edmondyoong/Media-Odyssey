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
import static org.mockito.Mockito.*;

/**
 * Unit tests for MediaRankingService
 * Score formula: views*1 + likes*10  (from Media.getTotalScore())
 * Display rating: mapped from totalScore via toDisplayRating()
 */
@ExtendWith(MockitoExtension.class)
class MediaRankingServiceTest {

    @Mock
    private UserInteractionRepository userInteractionRepository;

    @InjectMocks
    private MediaRankingService rankingService;

    // ─── normalizeMediaType Tests ──────────────────────────────────────────────

    @Test
    void normalizeMediaType_movie_returnsNormalizedString() {
        assertThat(rankingService.normalizeMediaType("MOVIE")).isEqualTo("MOVIE");
        assertThat(rankingService.normalizeMediaType("movies")).isEqualTo("MOVIE");
        assertThat(rankingService.normalizeMediaType("Movies")).isEqualTo("MOVIE");
    }

    @Test
    void normalizeMediaType_game_returnsNormalizedString() {
        assertThat(rankingService.normalizeMediaType("GAME")).isEqualTo("GAME");
        assertThat(rankingService.normalizeMediaType("games")).isEqualTo("GAME");
    }

    @Test
    void normalizeMediaType_music_returnsSong() {
        assertThat(rankingService.normalizeMediaType("MUSIC")).isEqualTo("SONG");
        assertThat(rankingService.normalizeMediaType("song")).isEqualTo("SONG");
        assertThat(rankingService.normalizeMediaType("SONGS")).isEqualTo("SONG");
    }

    @Test
    void normalizeMediaType_null_returnsEmptyString() {
        assertThat(rankingService.normalizeMediaType(null)).isEqualTo("");
    }

    @Test
    void normalizeMediaType_unknown_returnsUppercase() {
        assertThat(rankingService.normalizeMediaType("podcast")).isEqualTo("PODCAST");
    }

    // ─── getTop10 Tests ────────────────────────────────────────────────────────

    @Test
    void getTop10_noInteractions_returnsEmptyList() {
        when(userInteractionRepository.findTop10ByScore())
                .thenReturn(Collections.emptyList());

        List<RankedMediaResponse> result = rankingService.getTop10();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void getTop10_callsCorrectRepositoryMethod() {
        when(userInteractionRepository.findTop10ByScore())
                .thenReturn(Collections.emptyList());

        rankingService.getTop10();

        verify(userInteractionRepository, times(1)).findTop10ByScore();
        verify(userInteractionRepository, never()).findTop10ByScoreAndMediaType(any());
    }

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

    @Test
    void getTop10ByMediaType_movie_callsRepositoryWithNormalizedType() {
        when(userInteractionRepository.findTop10ByScoreAndMediaType("MOVIE"))
                .thenReturn(Collections.emptyList());

        rankingService.getTop10ByMediaType("movies");

        verify(userInteractionRepository, times(1))
                .findTop10ByScoreAndMediaType("MOVIE");
    }

    @Test
    void getTop10ByMediaType_music_normalizesToSong() {
        when(userInteractionRepository.findTop10ByScoreAndMediaType("SONG"))
                .thenReturn(Collections.emptyList());

        rankingService.getTop10ByMediaType("MUSIC");

        verify(userInteractionRepository, times(1))
                .findTop10ByScoreAndMediaType("SONG");
    }

    @Test
    void getTop10ByMediaType_unknownCategory_returnsEmptyList() {
        when(userInteractionRepository.findTop10ByScoreAndMediaType("PODCAST"))
                .thenReturn(Collections.emptyList());

        List<RankedMediaResponse> result = rankingService.getTop10ByMediaType("PODCAST");

        assertThat(result).isEmpty();
    }

    // ─── enrichScoreRows / RankedMediaResponse structure Tests ────────────────

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