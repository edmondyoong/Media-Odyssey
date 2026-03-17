package com.mo.mediaodyssey.community.favorite;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a media item (Movie, Game, or Song) in the Community Favourites page.
 *
 * Stored fields:
 *   - title, category, views, likes, likesLastWeek, ratingSum, ratingCount, createdAt
 *
 * Computed fields (not stored in DB, annotated with @Transient):
 *   - totalScore      : Point System ranking score
 *   - averageRating   : Average star rating (1–5)
 *   - trendingGrowth  : Weekly like growth percentage
 *   - trending        : Whether the item qualifies for the Fast-Rising badge
 */
@Entity
@Table(name = "media")
public class Media {

    // ── Primary Key ───────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Core Fields ───────────────────────────────────────────────────────────

    /** Display title of the media item (e.g. "Hollow Knight") */
    @Column(nullable = false)
    private String title;

    /** Media type: "Movie", "Game", or "Song" */
    @Column(nullable = false)
    private String category;

    /** Total number of times this item has been viewed */
    @Column(nullable = false)
    private int views;

    /** Total number of likes this item has received */
    @Column(nullable = false)
    private int likes;

    /**
     * Like count from 7 days ago.
     * Automatically updated every Sunday midnight by TrendingScheduler.
     * Used to calculate the weekly growth % for the Fast-Rising section.
     */
    @Column(nullable = false)
    private int likesLastWeek;

    /**
     * Sum of all submitted star ratings (1–5).
     * Divide by ratingCount to get the average.
     * Stored separately to allow incremental updates without recomputing.
     */
    @Column(nullable = false)
    private int ratingSum;

    /**
     * Total number of users who have submitted a star rating.
     * Used together with ratingSum to compute averageRating.
     */
    @Column(nullable = false)
    private int ratingCount;

    /** Timestamp of when this media item was first added */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // ── Lifecycle Callback ────────────────────────────────────────────────────

    /**
     * Called automatically before the entity is first persisted.
     * Ensures all numeric fields start at 0 and createdAt is set.
     * This means the Add Media form only needs title and category.
     */
    @PrePersist
    public void prePersist() {
        this.createdAt   = LocalDateTime.now();
        this.views       = 0;
        this.likes       = 0;
        this.likesLastWeek = 0;
        this.ratingSum   = 0;
        this.ratingCount = 0;
    }

    // ── Computed Fields (not stored in DB) ────────────────────────────────────

    /**
     * Point System ranking score.
     *
     * Formula: totalScore = (views × 1) + (likes × 10)
     *
     * Likes are weighted 10× because a like signals stronger user intent
     * than a passive view. This score determines the Top 10 ranking order.
     *
     * Note: Rating is displayed separately (like IMDB/Netflix)
     * rather than factored into the score, to avoid penalising
     * newer items with fewer ratings.
     */
    @Transient
    public int getTotalScore() {
        return (views * 1) + (likes * 10);
    }

    /**
     * Average star rating on a 1–5 scale.
     *
     * Returns 0.0 if no ratings have been submitted yet.
     * Rounded to 1 decimal place for display (e.g. 4.3).
     */
    @Transient
    public double getAverageRating() {
        if (ratingCount == 0) return 0.0;
        return Math.round((double) ratingSum / ratingCount * 10.0) / 10.0;
    }

    /**
     * Weekly like growth rate as a percentage.
     *
     * Formula: ((likes - likesLastWeek) / likesLastWeek) × 100
     *
     * Special cases:
     *   - If likesLastWeek is 0 and likes > 0: returns 100.0 (brand-new item gaining traction)
     *   - If both are 0: returns 0.0 (no activity)
     */
    @Transient
    public double getTrendingGrowth() {
        if (likesLastWeek == 0) return likes > 0 ? 100.0 : 0.0;
        return ((double)(likes - likesLastWeek) / likesLastWeek) * 100.0;
    }

    /**
     * Returns true if this item qualifies for the Fast-Rising trending badge.
     * Threshold: weekly like growth must be >= 10%.
     * Used by the Thymeleaf template to conditionally render the ▲ badge.
     */
    @Transient
    public boolean isTrending() {
        return getTrendingGrowth() >= 10.0;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getLikesLastWeek() { return likesLastWeek; }
    public void setLikesLastWeek(int likesLastWeek) { this.likesLastWeek = likesLastWeek; }

    public int getRatingSum() { return ratingSum; }
    public void setRatingSum(int ratingSum) { this.ratingSum = ratingSum; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}