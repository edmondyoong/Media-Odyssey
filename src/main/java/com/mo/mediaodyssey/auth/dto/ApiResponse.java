package com.mo.mediaodyssey.auth.dto;

public record AuthApiResponse(
        boolean success,
        String status,
        String message) {

    public static AuthApiResponse success(String code, String message) {
        return new AuthApiResponse(true, code, message);
    }

    public static AuthApiResponse error(String code, String message) {
        return new AuthApiResponse(false, code, message);
    }
}
