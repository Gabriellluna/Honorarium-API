package br.com.honorarium.honorarios_api.auth;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {}
