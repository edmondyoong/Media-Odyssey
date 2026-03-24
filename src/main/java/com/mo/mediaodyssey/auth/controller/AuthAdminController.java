package com.mo.mediaodyssey.auth.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.mo.mediaodyssey.auth.dto.AuthAdminApiResponse;
import com.mo.mediaodyssey.auth.dto.UserDto;
import com.mo.mediaodyssey.auth.services.AuthAdminService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin User creation logic for developmental use only.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthAdminController {

    @Value("${ALLOW_ADMIN_USER_CREATION:FALSE}")
    private String allowAdminUserCreation;

    @Value("${EMAIL_FROM:null}")
    private String adminEmail;

    @Autowired
    private AuthAdminService authAdminService;

    /**
     * Logic to create an Admin User and skip email verification.
     * 
     * For developmental use only. Must be enabled via environment variable. Once
     * enabled, access is unrestricted.
     * 
     * Email address and password is randomly assigned. $EMAIL_FROM is combined with
     * the randomly generated UUID. Password uses a different UUID.
     * 
     * @return "OK", Email, and Password in body upon success
     */
    @GetMapping("/createAdminUser")
    @Transactional
    public ResponseEntity<AuthAdminApiResponse> createAdminUser() {
        if (allowAdminUserCreation.toLowerCase().equals("TRUE".toLowerCase())) {

            String email = UUID.randomUUID().toString() + "-" + adminEmail;
            String password = UUID.randomUUID().toString();

            UserDto dto = new UserDto(email, password);
            authAdminService.createAdminUser(dto);

            // Return OK and generated credentials in a message body
            return ResponseEntity.ok(AuthAdminApiResponse.success(
                    "AUTH_ADMIN_CREATED",
                    "Admin created successfully.",
                    email,
                    password));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(AuthAdminApiResponse.error("AUTH_ADMIN_CREATION_DISABLED", "Admin creation is disabled"));
        }
    }

}
