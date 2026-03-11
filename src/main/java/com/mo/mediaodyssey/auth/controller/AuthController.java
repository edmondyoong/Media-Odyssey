package com.mo.mediaodyssey.auth.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mo.mediaodyssey.auth.dto.ResendVerifyTokenDto;
import com.mo.mediaodyssey.auth.dto.UserDto;
import com.mo.mediaodyssey.auth.dto.VerifyTokenDto;
import com.mo.mediaodyssey.auth.services.AuthService;
import com.mo.mediaodyssey.auth.services.VerificationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Temporary Admin User creation logic
    @Value("${ALLOW_ADMIN_USER_CREATION:FALSE}")
    private String allowAdminUserCreation;

    @Value("${EMAIL_FROM:null}")
    private String adminEmail;

    // Inspired by:
    // https://www.baeldung.com/spring-security-authentication-provider
    // https://www.djamware.com/post/secure-your-restful-api-with-spring-boot-35-jwt-and-mongodb
    // Debugging assisted by AI.

    @Autowired
    private AuthService authService;

    @Autowired
    private VerificationService verificationService;

    // AuthController(VerificationService verificationService) {
    // this.verificationService = verificationService;
    // }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<String> login(@RequestBody UserDto dto, HttpServletRequest request,
            HttpServletResponse response) {
        // Login the User
        Authentication authentication = authService.loginUser(dto);

        // Persist the login
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        new HttpSessionSecurityContextRepository().saveContext(context, request, response);

        // Return OK - successfully logged in
        return ResponseEntity.ok("OK");
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<String> register(@Valid @RequestBody UserDto dto) {
        authService.registerUser(dto);

        // Return OK - successfully registered
        return ResponseEntity.ok("OK");
    }

    @GetMapping(value = "/verify")
    @Transactional
    public ResponseEntity<Void> verify(@Valid @RequestParam("token") String token) {
        VerifyTokenDto dto = new VerifyTokenDto(token);
        verificationService.verifyUser(dto);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create("/auth/login"))
                .build();
    }

    @PostMapping(value = "/resend", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<String> resend(@Valid @RequestBody ResendVerifyTokenDto dto) {
        verificationService.resendVerification(dto);

        // Return OK - successfully resent
        return ResponseEntity.ok("OK");
    }

    /**
     * Temporary logic to create an Admin User and skip email verification.
     * 
     * Must be enabled via environment variable.
     * 
     * Email address and password is randomly assigned.
     * 
     * @return "OK", Email, and Password in body upon success
     */
    @GetMapping("/createAdminUser")
    @Transactional
    public ResponseEntity<String> createAdminUser(Authentication authentication) {
        if (allowAdminUserCreation.toLowerCase().equals("TRUE".toLowerCase())) {

            String id = UUID.randomUUID().toString();
            String email = id + "-" + adminEmail;
            String password = id;

            UserDto dto = new UserDto(email, password);
            authService.createAdminUser(dto);

            // Return OK, the email address, and password - successfully registered
            return ResponseEntity.ok("OK\n" +
                    "Email: " + email + " \n" +
                    "Password: " + password + " \n");
        } else {
            // return ResponseEntity
            // .status(HttpStatus.FORBIDDEN)
            // .build();
            return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
        }
    }

    // TODO: Completed?
    // AuthService, AuthController, GrantedAuthorities (Roles), Email
    // VerificationToken in VerificationService, Frontend (Thymeleaf templates)

}
