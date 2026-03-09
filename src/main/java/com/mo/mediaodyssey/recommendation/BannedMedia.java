package com.mo.mediaodyssey.recommendation;

import jakarta.persistence.*;

@Entity
@Table(name = "banned_media")
public class BannedMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mediaApiId;
    private String mediaType;

    public Long getId() { return id; }
    public String getMediaApiId() { return mediaApiId; }
    public String getMediaType() { return mediaType; }

    public void setMediaApiId(String mediaApiId) { this.mediaApiId = mediaApiId; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
}