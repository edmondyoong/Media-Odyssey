package com.mo.mediaodyssey.layout.models;

import java.util.List;

public class Movies {
    private Long id; 
    private List<Integer> genre_ids;
    private String title; 
    private String overview; 
    private String release_date;
    private String poster_path; 

    public Movies () {}

    public Movies (Long id, List<Integer> genres_ids, String title, String overview, String release_date, String poster_path){
        this.id = id;
        this.genre_ids = genres_ids; 
        this.title = title; 
        this.overview = overview; 
        this.release_date = release_date; 
        this.poster_path = poster_path;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getOverview() {
        return overview;
    }
    public void setOverview(String overview) {
        this.overview = overview;
    }
    public String getRelease_date() {
        return release_date;
    }
    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }
    public List<Integer> getGenre_ids() {
        return genre_ids;
    }
    public void setGenre_ids(List<Integer> genre_ids) {
        this.genre_ids = genre_ids;
    }
    public String getPoster_path() {
        return poster_path;
    }
    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }    

}
