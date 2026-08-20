package com.mhesus.api.clientes.application;

import com.mhesus.api.clientes.domain.Motocicleta;

public record PlacaDuplicadaResponse(String mensaje, Motocicleta motoExistente) {}
