package com.mo.mediaodyssey.community.favorite;

/**
 * DTO (Data Transfer Object) for a ranked media item on the Community Favourites page.
 *
 * Combines:
 *   - Interaction data from the UserInteraction table (score, mediaApiId, mediaType)
 *   - Media metadata fetched from external APIs (title, imageUrl, artist)
 *
 * Used by MediaRankingService and passed to the Thymeleaf template via the model.
 */
public class RankedMediaResponse {

    /** External API ID (e.g. TMDB movie ID, RAWG game ID, Spotify track ID) */
    private String mediaApiId;

    /** Display title of the media item */
    private String title;

    /** Artist name — only populated for songs (Spotify) */
    private String artist;

    /** Media type: "MOVIE", "GAME", or "SONG" */
    private String mediaType;

    /** Thumbnail image URL from the external API */
    private String imageUrl;

    /**
     * Point System score aggregated from all UserInteraction records.
     * Formula: (VIEW count × 1) + (LIKE count × 10)
     */
    private long totalScore;

    /** Weekly like count — used for the Fast-Rising trending section */
    private long weeklyLikes;

    // ── Constructor ───────────────────────────────────────────────────────────

    public RankedMediaResponse(String mediaApiId, String title, String artist,
                                String mediaType, String imageUrl,
                                long totalScore, long weeklyLikes) {
        this.mediaApiId  = mediaApiId;
        this.title       = title;
        this.artist      = artist;
        this.mediaType   = mediaType;
        this.imageUrl    = imageUrl;
        this.totalScore  = totalScore;
        this.weeklyLikes = weeklyLikes;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getMediaApiId()  { return mediaApiId; }
    public String getTitle()       { return title; }
    public String getArtist()      { return artist; }
    public String getMediaType()   { return mediaType; }
    public String getImageUrl()    { return imageUrl; }
    public long   getTotalScore()  { return totalScore; }
    public long   getWeeklyLikes() { return weeklyLikes; }
}