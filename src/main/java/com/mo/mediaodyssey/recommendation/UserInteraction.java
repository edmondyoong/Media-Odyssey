package com.mo.mediaodyssey.recommendation;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_interaction")
public class UserInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String mediaApiId;
    private String interactionType; // "VIEW", "LIKE"
    private String mediaType;       // "MOVIE", "GAME", "SONG"
    private LocalDateTime timestamp;

    @ElementCollection
    @CollectionTable(name = "user_interaction_genres", joinColumns = @JoinColumn(name = "interaction_id"))
    @Column(name = "genre")
    private List<String> genres;

    // getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getMediaApiId() { return mediaApiId; }
    public String getInteractionType() { return interactionType; }
    public String getMediaType() { return mediaType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public List<String> getGenres() { return genres; }

    // setters
    public void setUserId(Long userId) { this.userId = userId; }
    public void setMediaApiId(String mediaApiId) { this.mediaApiId = mediaApiId; }
    public void setInteractionType(String interactionType) { this.interactionType = interactionType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setGenres(List<String> genres) { this.genres = genres; }
}