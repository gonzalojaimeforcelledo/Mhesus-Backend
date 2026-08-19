package com.mhesus.api.almacen.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario {
    @Id
    public String id;

    @Column(name = "producto_id", nullable = false)
    public String productoId;

    @Column(nullable = false)
    public String tipo; // ingreso | salida | ajuste

    @Column(nullable = false)
    public int cantidad;

    @Column(name = "ot_id")
    public String otId;

    @Column(name = "usuario_id")
    public String usuarioId;

    @Column(name = "creado_en", nullable = false)
    public String creadoEn;

    public MovimientoInventario() {}
}
