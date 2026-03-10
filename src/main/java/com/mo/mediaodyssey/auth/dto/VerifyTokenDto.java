package com.mo.mediaodyssey.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyTokenDto(
        @NotBlank String token) {
}
