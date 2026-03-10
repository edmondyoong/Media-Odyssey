package com.mo.mediaodyssey.recommendation;

// represents one recommended media item returned to the frontend
public class RecommendationResponse {
    private String mediaApiId;
    private String title;
    private String mediaType;
    private String genre;
    private String imageUrl;
    private double score;

    public RecommendationResponse(String mediaApiId, String title, String mediaType,
                                   String genre, String imageUrl, double score) {
        this.mediaApiId = mediaApiId;
        this.title = title;
        this.mediaType = mediaType;
        this.genre = genre;
        this.imageUrl = imageUrl;
        this.score = score;
    }

    public String getMediaApiId() { return mediaApiId; }
    public String getTitle() { return title; }
    public String getMediaType() { return mediaType; }
    public String getGenre() { return genre; }
    public String getImageUrl() { return imageUrl; }
    public double getScore() { return score; }
}