package com.mo.mediaodyssey.layout.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class MappingController {

    /* This Controller is to navigates elements like create board icon, home icon, 
    elements in side bar, and elements view in singular board layout */

    /* HEADER ELEMENTS */
    @GetMapping("/homeReturn")
    public String homeReturn() {
        // return "boardsLayout/homePage";

        // TODO: redirect to '/' instead of rendering same page at '/' and
        // '/homeReturn'?
        return "redirect:/";
    }

    /* SIDE BAR ELEMENTS */

    @GetMapping("/socialTab")
    public String navToSocialTab() {
        return "social";
    }

    @GetMapping("/trendingTab")
    public String navToTrendingTab() {
        return "trending";
    }

    @GetMapping("/dashboardTab")
    public String navToDashboard() {
        return "/users/dashboard";
    }
}
