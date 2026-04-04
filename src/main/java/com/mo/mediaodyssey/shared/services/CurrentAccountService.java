package com.mo.mediaodyssey.shared.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import com.mo.mediaodyssey.shared.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.mo.mediaodyssey.auth.services.MOUserDetailsService;

@Service
public class CurrentAccountService {

    @Autowired
    private MOUserDetailsService userDetailsService;

    private Authentication isValidAuthentication(Authentication authentication) {
        if (authentication == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Current visitor has not authenticated with a valid account.");
        }

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication;
        } else {
            throw new AuthenticationCredentialsNotFoundException(
                    "Current visitor has not authenticated with a valid account.");
        }
    }

    public User getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) isValidAuthentication(authentication).getPrincipal();
    }

    public User getCurrentAccount(Authentication authentication) {
        return (User) isValidAuthentication(authentication).getPrincipal();
    }

    public void refreshPrincipal(Authentication authentication, HttpServletRequest request,
            HttpServletResponse response) {
        // Verify current authentication is valid.
        Authentication current = isValidAuthentication(authentication);

        // Refresh the authentication
        UserDetails refreshedUserDetails = userDetailsService.loadUserByUsername(current.getName());
        UsernamePasswordAuthenticationToken refreshedToken = new UsernamePasswordAuthenticationToken(
                refreshedUserDetails,
                current.getCredentials(), refreshedUserDetails.getAuthorities());
        refreshedToken.setDetails(current.getDetails());

        // Update the context in Spring Security and Spring Session
        SecurityContext refreshedContext = SecurityContextHolder.getContext();
        refreshedContext.setAuthentication(refreshedToken);
        SecurityContextHolder.setContext(refreshedContext);
        new HttpSessionSecurityContextRepository().saveContext(refreshedContext, request, response);
    }
}
