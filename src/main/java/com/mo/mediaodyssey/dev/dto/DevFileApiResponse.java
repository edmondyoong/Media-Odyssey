package com.mo.mediaodyssey.dev.dto;

import jakarta.validation.constraints.NotBlank;

public record DevFileApiResponse(
        boolean success,
        String status,
        String message,
        String key,
        String url) {

    public static DevFileApiResponse success(@NotBlank String status, @NotBlank String message, @NotBlank String key,
            @NotBlank String url) {
        return new DevFileApiResponse(true, status, message, key, url);
    }

    public static DevFileApiResponse error(@NotBlank String status, @NotBlank String message) {
        return new DevFileApiResponse(false, status, message, null, null);
    }
}
