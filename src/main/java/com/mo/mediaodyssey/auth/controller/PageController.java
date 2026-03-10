package com.mo.mediaodyssey.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/auth")
    public String authPage() {
        return "forward:/auth/index.html";
    }

    @GetMapping("/auth/login")
    public String loginPage() {
        return "forward:/auth/login/index.html";
    }

    @GetMapping("/auth/signup")
    public String registerPage() {
        return "forward:/auth/signup/index.html";
    }
}
