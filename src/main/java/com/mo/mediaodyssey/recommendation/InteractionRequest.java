package com.mo.mediaodyssey.recommendation;

import java.util.List;

// fields the frontend sends when recording a user interaction
public class InteractionRequest {
    private String mediaApiId;
    private String interactionType; // "VIEW" or "LIKE"
    private String mediaType;       // "MOVIE", "GAME", or "SONG"
    private List<String> genres;    // genres of the media item

    public String getMediaApiId() { return mediaApiId; }
    public String getInteractionType() { return interactionType; }
    public String getMediaType() { return mediaType; }
    public List<String> getGenres() { return genres; }

    public void setMediaApiId(String mediaApiId) { this.mediaApiId = mediaApiId; }
    public void setInteractionType(String interactionType) { this.interactionType = interactionType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public void setGenres(List<String> genres) { this.genres = genres; }
}