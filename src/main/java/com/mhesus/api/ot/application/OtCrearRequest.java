package com.mhesus.api.ot.application;

public record OtCrearRequest(
    String clienteId, String motoId, String nivelCombustible,
    String observacionCliente, String servicioARealizar, Integer kmActual, String fotoIngreso
) {}
