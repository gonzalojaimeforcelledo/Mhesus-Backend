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

    @Column(name = "precio_anterior", columnDefinition = "NUMERIC(10,2)")
    public Double precioAnterior;

    @Column(name = "descuento_maximo", columnDefinition = "NUMERIC(5,2)")
    public Double descuentoMaximo; // porcentaje (ej. 15 = hasta 15% de descuento)

    @Column(name = "stock_actual", nullable = false)
    public int stockActual;

    @Column(name = "stock_minimo", nullable = false)
    public int stockMinimo;

    public String lugar; // "Ubicación" en la interfaz — se mantiene el nombre de columna por compatibilidad

    // Compatibilidad con moto (opcional — no todos los productos son específicos de un modelo)
    @Column(name = "marca_moto")
    public String marcaMoto; // Bajaj | TVS | KTM

    @Column(name = "modelo_moto")
    public String modeloMoto; // ej. Pulsar, Apache RTR, Duke

    @Column(name = "submodelo_moto")
    public String submodeloMoto; // ej. NS200, 160 4V, 390

    @Column(name = "anio_desde")
    public Integer anioDesde;

    @Column(name = "anio_hasta")
    public Integer anioHasta;

    public Producto() {}
}
