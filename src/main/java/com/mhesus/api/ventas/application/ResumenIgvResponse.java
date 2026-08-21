package com.mhesus.api.ventas.application;

public record ResumenIgvResponse(
    double igvVentas, double igvCompras, double igvAPagar, boolean debePagar,
    boolean sinVentasEsteMes, boolean sinComprasEsteMes
) {}
