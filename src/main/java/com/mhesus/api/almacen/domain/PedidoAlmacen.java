package com.mhesus.api.almacen.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "pedidos_almacen")
public class PedidoAlmacen {
    @Id
    public String id;

    @Column(name = "ot_id", nullable = false)
    public String otId;

    @Column(nullable = false)
    public String estado; // Solicitado | Aprobado | Despachado | Cancelado

    @Column(name = "creado_por")
    public String creadoPor;

    @Column(name = "creado_en", nullable = false)
    public String creadoEn;

    @Column(name = "foto_despacho", columnDefinition = "TEXT")
    public String fotoDespacho;

    public PedidoAlmacen() {}
}
