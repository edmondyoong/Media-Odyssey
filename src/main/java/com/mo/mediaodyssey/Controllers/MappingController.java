package com.mo.mediaodyssey.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MappingController {
    
    @GetMapping("/createBoard")
    public String createBoard() {
        return "createBoard";
    }

    @GetMapping("/homeReturn")
    public String homeReturn() {
        return "/boardsLayout/homePage";
    }
}
