package com.mo.mediaodyssey.layout.controllers;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.mo.mediaodyssey.shared.model.User;

@ControllerAdvice
public class FragmentsControllerAdvice {

    @ModelAttribute("user")
    public User getCurrentUser(Principal principal, Authentication authentication) {
        if (principal == null)
            return null;

        User user = (User) authentication.getPrincipal();

        return user;
    }
}
