package com.mhesus.api.deudas.application;

public record DeudaRequest(
    String tipo, String nombre, String descripcion, String clienteId,
    double montoOriginal, String fechaVencimiento
) {}
