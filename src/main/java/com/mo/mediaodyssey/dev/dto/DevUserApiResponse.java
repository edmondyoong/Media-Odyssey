package com.mo.mediaodyssey.dev.dto;

import jakarta.validation.constraints.NotBlank;

public record DevUserApiResponse(
        boolean success,
        String status,
        String message,
        String email,
        String password) {

    public static DevUserApiResponse success(@NotBlank String status, @NotBlank String message,
            @NotBlank String email, @NotBlank String password) {
        return new DevUserApiResponse(true, status, message, email, password);
    }

    public static DevUserApiResponse error(@NotBlank String status, @NotBlank String message) {
        return new DevUserApiResponse(false, status, message, null, null);
    }
}
