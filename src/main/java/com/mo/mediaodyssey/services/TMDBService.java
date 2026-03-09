package com.mo.mediaodyssey.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TMDBService {
    
    @Value("${tmdb.api.key}")
    private String apiKey;

    public String getTitle() {
        String url = "https://api.themoviedb.org/3/movie/popular?api_key=" + apiKey; 

        RestTemplate restTemplate = new RestTemplate(); 
        return restTemplate.getForObject(url, String.class);
    }
}
