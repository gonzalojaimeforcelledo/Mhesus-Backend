package com.mhesus.api.ofertas.application;

import java.util.List;

public record OfertaRequest(String nombre, String descripcion, double precioOferta, List<ItemOfertaDto> items) {}
