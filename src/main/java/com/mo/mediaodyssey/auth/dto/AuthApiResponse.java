package com.mo.mediaodyssey.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthApiResponse(
        boolean success,
        String status,
        String message) {

    public static AuthApiResponse success(@NotBlank String status, @NotBlank String message) {
        return new AuthApiResponse(true, status, message);
    }

    public static AuthApiResponse error(@NotBlank String status, @NotBlank String message) {
        return new AuthApiResponse(false, status, message);
    }
}
