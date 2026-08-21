package com.mhesus.api.ventas.application;

import java.util.List;

public record VentaRequest(
    String tipo, String serie, String otId, String clienteId,
    String clienteNombre, String clienteDocumento, List<ItemVentaDto> items
) {}
