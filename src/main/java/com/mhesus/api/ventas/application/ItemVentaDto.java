package com.mhesus.api.ventas.application;

public record ItemVentaDto(String descripcion, double cantidad, double precioUnitario, String productoId, String ofertaId) {}
