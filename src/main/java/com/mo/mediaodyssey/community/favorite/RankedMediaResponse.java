package com.mo.mediaodyssey.community.favorite;

/**
 * DTO used by the Community Favourites page.
 *
 * Combines:
 * - metadata from external APIs
 * - score/trending information from UserInteraction
 * - rating information from Media table
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

    /** score = views * 1 + likes * 10 */
    private long totalScore;

    /** Likes collected in the past 7 days */
    private long weeklyLikes;

    /** Average star rating */
    private double averageRating;

    /** Number of submitted ratings */
    private int ratingCount;

    public RankedMediaResponse(String mediaApiId,
                               String title,
                               String artist,
                               String mediaType,
                               String imageUrl,
                               long totalScore,
                               long weeklyLikes,
                               double averageRating,
                               int ratingCount) {
        this.mediaApiId = mediaApiId;
        this.title = title;
        this.artist = artist;
        this.mediaType = mediaType;
        this.imageUrl = imageUrl;
        this.totalScore = totalScore;
        this.weeklyLikes = weeklyLikes;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
    }

    public String getMediaApiId() {
        return mediaApiId;
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

    public long getWeeklyLikes() {
        return weeklyLikes;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }
}