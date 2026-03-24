package com.mo.mediaodyssey.layout.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MappingController {

    /* This Controller is to navigates elements like create board icon, home icon, 
    elements in side bar, and elements view in singular board layout */

    /* HEADER ELEMENTS */

    /* homeReturn is used for a en element in header, specifically home icon. 
    homeReturn will direct user back to the homePage no matter which pages they are on.
    This ensures the convenience for the user. */
    @GetMapping("/homeReturn")
    public String homeReturn() {
        return "boardsLayout/homePage";
    }

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
