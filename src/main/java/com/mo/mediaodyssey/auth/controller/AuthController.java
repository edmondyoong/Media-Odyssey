package com.mo.mediaodyssey.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mo.mediaodyssey.auth.dto.ResendVerifyTokenDto;
import com.mo.mediaodyssey.auth.dto.UserDto;
import com.mo.mediaodyssey.auth.dto.VerifyTokenDto;
import com.mo.mediaodyssey.auth.services.AuthService;
import com.mo.mediaodyssey.auth.services.VerificationService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final VerificationService verificationService;

    // Inspiried by:
    // https://www.baeldung.com/spring-security-authentication-provider
    // https://www.djamware.com/post/secure-your-restful-api-with-spring-boot-35-jwt-and-mongodb
    // Debugging assisted by AI.

    @Autowired
    private AuthService authService;

    AuthController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<String> login(@RequestBody UserDto dto) {
        authService.loginUser(dto);
        return ResponseEntity.ok("Login successful");
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<String> register(@Valid @RequestBody UserDto dto) {
        authService.registerUser(dto);
        return ResponseEntity.ok("Successfully registered. Please check your email.");
    }

    @PostMapping("/verify")
    @Transactional
    public ResponseEntity<String> verify(@Valid @RequestBody VerifyTokenDto dto) {
        verificationService.verifyUser(dto);
        return ResponseEntity.ok("User confirmed. Thank you!");
    }

    @PostMapping("/resend")
    @Transactional
    public ResponseEntity<String> resend(@Valid @RequestBody ResendVerifyTokenDto dto) {
        verificationService.resendVerification(dto);
        return ResponseEntity.ok("Verification email resent.");
    }

    // TODO: AuthService, AuthController, GrantedAuthorities (Roles), Email
    // VerificationToken in VerificationService, Frontend (Thymeleaf templates)

}
