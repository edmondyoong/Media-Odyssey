package com.mo.mediaodyssey.dev.dto;

import jakarta.validation.constraints.NotBlank;

public record DevAccountApiResponse(
        boolean success,
        String status,
        String message,
        String email,
        String password) {

    public static DevAccountApiResponse success(@NotBlank String status, @NotBlank String message,
            @NotBlank String email, @NotBlank String password) {
        return new DevAccountApiResponse(true, status, message, email, password);
    }

    public static DevAccountApiResponse error(@NotBlank String status, @NotBlank String message) {
        return new DevAccountApiResponse(false, status, message, null, null);
    }
}
