package com.mo.mediaodyssey.repository;

import com.mo.mediaodyssey.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for the Media entity.
 *
 * Extends JpaRepository to inherit standard CRUD operations.
 * Custom queries use JPQL (not native SQL) for portability.
 *
 * Point System formula used in ranking queries:
 *   totalScore = (views × 1) + (likes × 10)
 */
public interface MediaRepository extends JpaRepository<Media, Long> {

    // ── Sort Queries ──────────────────────────────────────────────────────────

    /** Returns all media sorted by likes descending. Used for "Most Liked" view. */
    List<Media> findAllByOrderByLikesDesc();

    /** Returns all media sorted by views descending. Used for "Most Viewed" view. */
    List<Media> findAllByOrderByViewsDesc();

    // ── Top 10 Ranking Queries ────────────────────────────────────────────────

    /**
     * Returns the Top 10 media items ranked by Point System score.
     * Used for the default "All" tab on the community page.
     *
     * Formula: totalScore = (views * 1) + (likes * 10)
     */
    @Query("SELECT m FROM Media m ORDER BY (m.views * 1 + m.likes * 10) DESC LIMIT 10")
    List<Media> findTop10ByScore();

    /**
     * Returns the Top 10 media items for a specific category, ranked by Point System score.
     * Used when the user clicks the Movies, Games, or Songs tab.
     *
     * @param category one of "Movie", "Game", or "Song"
     */
    @Query("SELECT m FROM Media m WHERE m.category = :category ORDER BY (m.views * 1 + m.likes * 10) DESC LIMIT 10")
    List<Media> findTop10ByScoreAndCategory(@Param("category") String category);

    // ── Trending Query ────────────────────────────────────────────────────────

    /**
     * Returns the Top 5 fastest-rising media items ranked by weekly like growth.
     * Growth is calculated as (likes - likesLastWeek).
     *
     * likesLastWeek is automatically updated every Sunday midnight by TrendingScheduler.
     * Items with the greatest absolute growth this week appear first.
     */
    @Query("SELECT m FROM Media m ORDER BY (m.likes - m.likesLastWeek) DESC LIMIT 5")
    List<Media> findTop5Trending();

    // ── Rating Query ──────────────────────────────────────────────────────────

    /**
     * Returns the Top 10 media items ranked by average star rating.
     * Only includes items that have received at least one rating (ratingCount > 0).
     * Used for the "Top Rated" sort option.
     *
     * Formula: avgRating = ratingSum / ratingCount
     */
    @Query("SELECT m FROM Media m WHERE m.ratingCount > 0 ORDER BY (CAST(m.ratingSum AS double) / m.ratingCount) DESC LIMIT 10")
    List<Media> findTop10ByRating();
}