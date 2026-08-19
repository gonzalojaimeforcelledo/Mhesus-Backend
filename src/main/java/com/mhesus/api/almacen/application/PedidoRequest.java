package com.mhesus.api.almacen.application;

import java.util.List;

public record PedidoRequest(List<ItemPedido> items) {}
