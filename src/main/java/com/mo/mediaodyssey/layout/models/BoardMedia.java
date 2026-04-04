package com.mo.mediaodyssey.layout.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class BoardMedia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @ManyToOne
    @JoinColumn(name="board_id", nullable = false) 
    private Boards board; 

    // Store the Media in Boards Model: (Only movie media is available to store for now...)
    private Long mediaApiId;
    private String media_type; 
    private String media_title; 
    private String media_poster_path; 
    private String media_genres;

    public BoardMedia (){}

    public BoardMedia(Long id, Boards board, Long mediaApiId, String media_type, String media_title, String media_poster_path, String media_genres){
        this.id = id; 
        this.board = board; 
        this.mediaApiId = mediaApiId; 
        this.media_type = media_type; 
        this.media_title = media_title; 
        this.media_poster_path = media_poster_path; 
        this.media_genres = media_genres;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boards getBoard() {
        return board;
    }

    public void setBoard(Boards board) {
        this.board = board;
    }

    public Long getMediaApiId() {
        return mediaApiId;
    }

    public void setMediaApiId(Long mediaApiId) {
        this.mediaApiId = mediaApiId;
    }

    public String getMedia_type() {
        return media_type;
    }

    public void setMedia_type(String media_type) {
        this.media_type = media_type;
    }

    public String getMedia_title() {
        return media_title;
    }

    public void setMedia_title(String media_title) {
        this.media_title = media_title;
    }

    public String getMedia_poster_path() {
        return media_poster_path;
    }

    public void setMedia_poster_path(String media_poster_path) {
        this.media_poster_path = media_poster_path;
    }

    public String getMedia_genres() {
        return media_genres;
    }

    public void setMedia_genres(String media_genres) {
        this.media_genres = media_genres;
    } 

    
}
