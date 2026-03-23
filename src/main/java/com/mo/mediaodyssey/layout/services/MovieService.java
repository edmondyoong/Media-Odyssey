package com.mo.mediaodyssey.layout.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mo.mediaodyssey.layout.DTO.MovieResponse;

@Service
public class MovieService {

    @Value("${tmdb.api.key}")
    private String tmdbKey; 

    private final RestTemplate restTemplate; 

    public MovieService (RestTemplate restTemplate) {
        this.restTemplate = restTemplate; 
    }

    /* Find movies:
    ** ==== GetMovieById: 
    *** This function is used to translate the web result to json object.
    *** This function would be used most for movies that are already displayed on the browser.
    *** Because fetching and finding by movieId would be more accurate for data displayed in movieDisplay.html
    *** Main focus: title, poster_path, genres (map), overview, released date, ...
    */
    public MovieResponse getMovieById(Long movieId) {
        String url = "https://api.themoviedb.org/3/movie/" + movieId 
                    + "?api_key=" + tmdbKey; 

        return restTemplate.getForObject(url, MovieResponse.class);
    }
    
}
