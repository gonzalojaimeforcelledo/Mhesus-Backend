package com.mhesus.api.ventas.application;

import java.util.Map;

public record ResumenDiaResponse(
    double total, int emisiones, double promedio, double igv,
    Map<String, ConteoTipo> porTipo, String ultimaEmision
) {
    public record ConteoTipo(double monto, int cantidad) {}
}
