package com.mo.mediaodyssey.community.favorite;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Media entity used for:
 * - cached metadata
 * - rating storage
 *
 * externalApiId examples:
 * - TMDB movie id
 * - RAWG game id
 * - Spotify track id
 *
 * category:
 * - MOVIE / GAME / SONG
 */
@Entity
@Table(
        name = "media",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"external_api_id", "category"})
        }
)
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** External API id (TMDB / RAWG / Spotify) */
    @Column(name = "external_api_id")
    private String externalApiId;

    @Column(nullable = false)
    private String title;

    /** MOVIE / GAME / SONG */
    @Column(nullable = false)
    private String category;

    /** Artist for songs, blank for movies/games */
    @Column(nullable = false)
    private String artist = "";

    /** Cached image URL */
    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(nullable = false)
    private int views;

    @Column(nullable = false)
    private int likes;

    @Column(nullable = false)
    private int likesLastWeek;

    @Column(nullable = false)
    private int ratingSum;

    @Column(nullable = false)
    private int ratingCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Derived score used by Top Score ranking.
     */
    @Transient
    public int getTotalScore() {
        return (views * 1) + (likes * 10);
    }

    /**
     * Derived average star rating.
     */
    @Transient
    public double getAverageRating() {
        if (ratingCount == 0) return 0.0;
        return Math.round(((double) ratingSum / ratingCount) * 10.0) / 10.0;
    }

    /**
     * Derived trending growth rate.
     */
    @Transient
    public double getTrendingGrowth() {
        if (likesLastWeek == 0) return likes > 0 ? 100.0 : 0.0;
        return ((double) (likes - likesLastWeek) / likesLastWeek) * 100.0;
    }

    @Transient
    public boolean isTrending() {
        return getTrendingGrowth() >= 10.0;
    }

    public Long getId() {
        return id;
    }

    public String getExternalApiId() {
        return externalApiId;
    }

    public void setExternalApiId(String externalApiId) {
        this.externalApiId = externalApiId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist == null ? "" : artist;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getLikesLastWeek() {
        return likesLastWeek;
    }

    public void setLikesLastWeek(int likesLastWeek) {
        this.likesLastWeek = likesLastWeek;
    }

    public int getRatingSum() {
        return ratingSum;
    }

    public void setRatingSum(int ratingSum) {
        this.ratingSum = ratingSum;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}