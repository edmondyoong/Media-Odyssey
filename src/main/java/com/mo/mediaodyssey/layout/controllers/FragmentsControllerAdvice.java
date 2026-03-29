package com.mo.mediaodyssey.layout.controllers;

import com.mo.mediaodyssey.auth.model.User;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class FragmentsControllerAdvice {

    @ModelAttribute("user")
    public User getCurrentUser (Principal principal, Authentication authentication) {
        if(principal == null) return null; 

        User user = (User) authentication.getPrincipal(); 

        return user; 
    }
}
