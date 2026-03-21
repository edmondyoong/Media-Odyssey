package com.mo.mediaodyssey.layout.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MappingController {

    @GetMapping("/homeReturn")
    public String homeReturn() {
        return "boardsLayout/homePage";
    }

    @GetMapping("/socialTab")
    public String navToSocialTab() {
        return "boardsLayout/features/social";
    }

    @GetMapping("/dashboardTab")
    public String navToDashboard() {
        return "users/dashboard";
    }

    /**
     * Redirect old trending route to the real community controller route.
     *
     * This fixes the earlier issue where opening /trendingTab directly
     * bypassed the controller logic and therefore broke filtering/sorting.
     */
    @GetMapping("/trendingTab")
    public String navToTrendingTab() {
        return "redirect:/community";
    }
}