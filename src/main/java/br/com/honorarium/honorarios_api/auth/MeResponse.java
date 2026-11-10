package br.com.honorarium.honorarios_api.auth;

import java.util.UUID;

public record MeResponse(UUID id, String email, String nome) {}
