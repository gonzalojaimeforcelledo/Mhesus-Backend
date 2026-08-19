package com.mhesus.api.auth.application;

public record LoginResponse(String token, UsuarioDto usuario) {}
