package com.mo.mediaodyssey.community.favorite;

/**
 * DTO used by the Community Favourites page.
 *
 * Iteration 3:
 * - use totalScore internally for ranking (not seen)
 * - expose displayRating for star-based UI
 * - expose weeklyLikes for Fast-Rising section
 */
public class RankedMediaResponse {

    /** External API id (TMDB / RAWG / Spotify) */
    private String mediaApiId;

    /** Display title */
    private String title;

    /** Artist name (mainly for songs) */
    private String artist;

    /** MOVIE / GAME / SONG */
    private String mediaType;

    /** Poster / thumbnail / album image */
    private String imageUrl;

    /** Internal score = views * 1 + likes * 10 */
    private long totalScore;

    /** UI-only star rating derived from totalScore */
    private double displayRating;

    /**
     * Number of LIKE Interactions in the past 7 days — used by Fast-Rising section
     */
    private long weeklyLikes;

    /** Constructor for Top 10 ranked items (weeklyLikes dafults to 0) */
    public RankedMediaResponse(String mediaApiId,
            String title,
            String artist,
            String mediaType,
            String imageUrl,
            long totalScore,
            double displayRating) {
        this.mediaApiId = mediaApiId;
        this.title = title;
        this.artist = artist;
        this.mediaType = mediaType;
        this.imageUrl = imageUrl;
        this.totalScore = totalScore;
        this.displayRating = displayRating;
        this.weeklyLikes = 0;
    }

    public String getMediaApiId() {
        return mediaApiId;
    }

    /** Constructor for Fast-Rising items */
    public RankedMediaResponse(String mediaApiId,
            String title,
            String artist,
            String mediaType,
            String imageUrl,
            long totalScore,
            double displayRating,
            long weeklyLikes) {
        this.mediaApiId = mediaApiId;
        this.title = title;
        this.artist = artist;
        this.mediaType = mediaType;
        this.imageUrl = imageUrl;
        this.totalScore = totalScore;
        this.displayRating = displayRating;
        this.weeklyLikes = weeklyLikes;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getTotalScore() {
        return totalScore;
    }

    public double getDisplayRating() {
        return displayRating;
    }

    public long getWeeklyLikes() {
        return weeklyLikes;
    }
}