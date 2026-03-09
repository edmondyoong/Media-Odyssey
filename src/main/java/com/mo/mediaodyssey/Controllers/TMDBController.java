package com.mo.mediaodyssey.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.mo.mediaodyssey.services.TMDBService;

import ch.qos.logback.core.model.Model;

@Controller
public class TMDBController {
    
    private final TMDBService tmdbService; 

    public TMDBController (TMDBService tmdbService) {
        this.tmdbService = tmdbService; 
    }
}
