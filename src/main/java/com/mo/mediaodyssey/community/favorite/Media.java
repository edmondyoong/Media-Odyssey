package com.mo.mediaodyssey.community.favorite;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Media entity used for cached metadata if needed by community/favorite.
 *
 * Current iteration:
 * - kept lightweight
 * - artist is transient because the current DB schema does not include artist
 *
 * Later iteration:
 * - if the DB schema is updated, artist can be persisted safely
 * - fast-rising related counters can also be used more actively
 */
@Entity
@Table(name = "media", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "external_api_id", "category" })
})
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

    /**
     * Current DB schema does not include artist column.
     * Keep it transient for now to avoid runtime SQL errors.
     *
     * TODO (later iteration):
     * After dealing with API problem, Transient will be changed or not
     */
    @Transient
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

    @Transient
    public int getTotalScore() {
        return (views * 1) + (likes * 10);
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