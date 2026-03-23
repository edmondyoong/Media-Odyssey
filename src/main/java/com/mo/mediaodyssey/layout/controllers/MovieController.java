package com.mo.mediaodyssey.layout.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.mo.mediaodyssey.layout.DTO.MovieResponse;
import com.mo.mediaodyssey.layout.services.MovieService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class MovieController {

    private final MovieService movieService; 

    public MovieController (MovieService movieService) {
        this.movieService = movieService; 
    }

    @GetMapping("/movie/{id}")
    public String getMovie(@PathVariable Long id, 
                        Model model, RedirectAttributes redirectAttributes) {

        MovieResponse movie = movieService.getMovieById(id); 

        model.addAttribute("movie", movie);

        return "boardsLayout/mediaDisplay/movieDisplay";
    }
    
    
}
