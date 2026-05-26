package com.example.Stq.autenticacao.application.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {}
