package com.mo.mediaodyssey.Responses;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mo.mediaodyssey.models.Movies;

public class MoviesResponse {

    @JsonProperty("results")
    private List<Movies> movieResults;

    public List<Movies> getResults() {
        return movieResults;
    }

    public void setResults(List<Movies> movieResults) {
        this.movieResults = movieResults;
    }
}