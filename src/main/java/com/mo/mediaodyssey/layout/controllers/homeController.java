package com.mo.mediaodyssey.layout.controllers;

import java.util.Collections;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mo.mediaodyssey.auth.model.User;
import com.mo.mediaodyssey.auth.repository.UserRepository;
import com.mo.mediaodyssey.layout.services.BoardsService;

@Controller
public class homeController {

    /* After users logged in, they will be direct to homePage.html */

    private final BoardsService boardsService;
    private final UserRepository userRepository;

    public homeController(BoardsService boardsService, UserRepository userRepository) {
        this.boardsService = boardsService;
        this.userRepository = userRepository; 
    }

    /* 
    * homePage Mapping: 
    ** Double check user's authentication.
    ** Find the logged in user by Email, and check all their boards (created + joined [not yet implemented])
    ** If no bards found, display nothing. Otherwise, Display all the boards.

    *** In the case of not finding any boards, or user not authenticated: 
    ** homePage.html will not displayed any theme boards in "Jounreys you have joined".
    ** But most likely, auth section will prevent unauthenticated users to join. This is just to double check.
    */
    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        if(authentication!=null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName(); 
            User user = userRepository.findByEmail(userEmail).orElse(null); 

            if (user != null) { model.addAttribute("boards", boardsService.findAllBoards()); }
            else { model.addAttribute("boards", Collections.emptyList());}
        } else {
            model.addAttribute("boards", Collections.emptyList());
        }

        return "boardsLayout/homePage";
    }
}
