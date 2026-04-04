package com.mo.mediaodyssey.community.favorite;

/**
 * DTO used by the Community Favourites page.
 *
 * Popularity Score = views * 1 + likes * 10
 * Likes and views are exposed separately for display.
 */
public class RankedMediaResponse {

    /** External API id (TMDB / RAWG / Last.fm URL) */
    private String mediaApiId;

    /** Display title */
    private String title;

    /** Artist name (mainly for songs) */
    private String artist;

    /** MOVIE / GAME / SONG */
    private String mediaType;

    /** Poster / thumbnail / album image */
    private String imageUrl;

    /** Popularity score = views * 1 + likes * 10 */
    private long totalScore;

    /** Number of LIKE interactions */
    private long likes;

    /** Number of VIEW interactions */
    private long views;

    /**
     * Number of LIKE interactions in the past 7 days — used by Fast-Rising section.
     */
    private long weeklyLikes;

    /** Constructor for Top 10 ranked items */
    public RankedMediaResponse(String mediaApiId,
            String title,
            String artist,
            String mediaType,
            String imageUrl,
            long totalScore,
            long likes,
            long views) {
        this.mediaApiId = mediaApiId;
        this.title = title;
        this.artist = artist;
        this.mediaType = mediaType;
        this.imageUrl = imageUrl;
        this.totalScore = totalScore;
        this.likes = likes;
        this.views = views;
        this.weeklyLikes = 0;
    }

    /** Constructor for Fast-Rising items */
    public RankedMediaResponse(String mediaApiId,
            String title,
            String artist,
            String mediaType,
            String imageUrl,
            long totalScore,
            long likes,
            long views,
            long weeklyLikes) {
        this.mediaApiId = mediaApiId;
        this.title = title;
        this.artist = artist;
        this.mediaType = mediaType;
        this.imageUrl = imageUrl;
        this.totalScore = totalScore;
        this.likes = likes;
        this.views = views;
        this.weeklyLikes = weeklyLikes;
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

    public long getLikes() {
        return likes;
    }

    public long getViews() {
        return views;
    }

    public long getWeeklyLikes() {
        return weeklyLikes;
    }
}