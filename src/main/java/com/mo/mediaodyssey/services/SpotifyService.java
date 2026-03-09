package com.mo.mediaodyssey.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SpotifyService {
    
    @Value("${spotify.client.id}")
    private String clientId; 

    @Value("${spotify.client.secret}")
    private String clientSecret; 
}
