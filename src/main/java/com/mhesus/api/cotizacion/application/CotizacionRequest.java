package com.mhesus.api.cotizacion.application;

import java.util.List;

public record CotizacionRequest(List<ItemCotizacionDto> detalle) {}
