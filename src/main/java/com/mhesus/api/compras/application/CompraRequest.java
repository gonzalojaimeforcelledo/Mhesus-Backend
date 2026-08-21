package com.mhesus.api.compras.application;

public record CompraRequest(String proveedor, String descripcion, String numeroComprobante, double montoTotal, String fecha) {}
