package com.mhesus.api.almacen.application;

public record ProductoRequest(
    String codigo, String codigoBarras, String nombre, String categoria,
    double precio, int stockActual, int stockMinimo, String lugar
) {}
