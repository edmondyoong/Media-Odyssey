package com.mo.mediaodyssey.community.favorite;

/**
 * DTO used by the Community Favourites page.
 *
 * Current iteration:
 * - use totalScore internally for ranking
 * - expose displayRating for star-based UI
 *
 * Later iteration ideas:
 * - add fast-rising / trendingGrowth fields
 * - add user-submitted rating fields if needed
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

    public double getDisplayRating() {
        return displayRating;
    }
}