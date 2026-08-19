package com.mhesus.api.almacen.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "pedido_detalle")
public class PedidoDetalle {
    @Id
    public String id;

    @Column(name = "pedido_id", nullable = false)
    public String pedidoId;

    @Column(name = "producto_id", nullable = false)
    public String productoId;

    @Column(name = "cantidad_solicitada", nullable = false)
    public int cantidadSolicitada;

    @Column(name = "cantidad_despachada", nullable = false)
    public int cantidadDespachada;

    public PedidoDetalle() {}
}
