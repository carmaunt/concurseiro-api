package br.com.concurseiro.api.usuarios.dto;

public record RefreshResponse(
        String accessToken,
        String refreshToken
) {}