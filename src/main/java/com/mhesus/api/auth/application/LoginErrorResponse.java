package com.mhesus.api.auth.application;

public record LoginErrorResponse(String mensaje, Long bloqueadoHasta) {}
