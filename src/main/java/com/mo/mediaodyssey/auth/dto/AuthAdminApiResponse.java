package com.mo.mediaodyssey.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthAdminApiResponse(
        boolean success,
        String status,
        String message,
        String email,
        String password) {

    public static AuthAdminApiResponse success(@NotBlank String status, @NotBlank String message,
            @NotBlank String email, @NotBlank String password) {
        return new AuthAdminApiResponse(true, status, message, email, password);
    }

    public static AuthAdminApiResponse error(@NotBlank String status, @NotBlank String message) {
        return new AuthAdminApiResponse(false, status, message, null, null);
    }
}
