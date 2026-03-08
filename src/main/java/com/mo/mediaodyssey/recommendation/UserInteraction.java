package com.mo.mediaodyssey.recommendation;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class UserInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String mediaApiId;
    private String interactionType;
    private String mediaType;
    private LocalDateTime timestamp;

    // getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getMediaApiId() { return mediaApiId; }
    public String getInteractionType() { return interactionType; }
    public String getMediaType() { return mediaType; }
    public LocalDateTime getTimestamp() { return timestamp; }
}