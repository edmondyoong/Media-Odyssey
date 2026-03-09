package com.mo.mediaodyssey.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class homeController {

    /* MAP THE INITIAL LOCALHOST:8080 DIRECTLY TO HOMEPAGE (WILL NEED REDIRECT) */
    
    @GetMapping("/")
    public String home(){
        return "/boardsLayout/homePage";
    }
}
