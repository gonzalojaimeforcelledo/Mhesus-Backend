package com.mhesus.api.almacen.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {
    @Id
    public String id;

    @Column(nullable = false, unique = true)
    public String codigo;

    @Column(name = "codigo_barras")
    public String codigoBarras;

    @Column(nullable = false)
    public String nombre;

    public String categoria;

    @Column(nullable = false, columnDefinition = "NUMERIC(10,2)")
    public double precio;

    @Column(name = "stock_actual", nullable = false)
    public int stockActual;

    @Column(name = "stock_minimo", nullable = false)
    public int stockMinimo;

    public String lugar;

    public Producto() {}
}
