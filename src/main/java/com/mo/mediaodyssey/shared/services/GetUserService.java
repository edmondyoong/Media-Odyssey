package com.mo.mediaodyssey.shared.services;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.mo.mediaodyssey.shared.model.User;

@Service
public class GetUserService {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return (User) authentication.getPrincipal();
        } else {
            throw new AuthenticationCredentialsNotFoundException(
                    "Current user has not authenticated with a valid account.");
        }
    }

    public User getCurrentUser(Authentication authentication) {
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return (User) authentication.getPrincipal();
        } else {
            throw new AuthenticationCredentialsNotFoundException(
                    "Current user has not authenticated with a valid account.");
        }
    }
}
