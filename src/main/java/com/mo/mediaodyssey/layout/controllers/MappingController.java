package com.mo.mediaodyssey.layout.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class MappingController {

    /* This Controller is to navigates elements like create board icon, 
    elements in side bar, and elements view in singular board layout */

    /* SIDE BAR ELEMENTS */

    /* Bring user to the page specifically for social feature.
    Social feature needs to be discussed more specifically tho.*/
    @GetMapping("/socialTab")
    public String navToSocialTab() {
        return "boardsLayout/features/social";
    }

    /* Temporary, social feature is in dashboard so there will be a dashboard element in sidebar.*/
    @GetMapping("/dashboardTab")
    public String navToDashboard() {
        return "users/dashboard";
    }

    /* Bring user to the page specifically for trending feature */
    @GetMapping("/trendingTab")
    public String navToTrendingTab() {
        return "boardsLayout/features/trending";
    }
}
