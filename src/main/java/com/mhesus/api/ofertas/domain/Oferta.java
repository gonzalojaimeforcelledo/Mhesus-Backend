package com.mhesus.api.ofertas.domain;

import jakarta.persistence.*;

/**
 * Una oferta es un combo de productos del catálogo (ej. "Kit de mantenimiento:
 * aceite + filtro") que se vende como un solo ítem a un precio especial. No
 * tiene stock propio — al venderse, el stock se descuenta de cada producto
 * que la compone (ver VentaService), no de la oferta en sí.
 */
@Entity
@Table(name = "ofertas")
public class Oferta {
    @Id
    public String id;

    @Column(nullable = false, length = 200)
    public String nombre;

    @Column(length = 500)
    public String descripcion;

    @Column(name = "precio_oferta", nullable = false, columnDefinition = "NUMERIC(10,2)")
    public double precioOferta;

    /** Lista de {productoId, cantidad} serializada como JSON — qué productos y en qué cantidad componen la oferta. */
    @Column(name = "items_json", nullable = false, columnDefinition = "TEXT")
    public String itemsJson;

    @Column(nullable = false)
    public boolean activo = true;

    @Column(name = "creado_por", nullable = false)
    public String creadoPor;

    @Column(name = "creado_en", nullable = false)
    public String creadoEn;

    public Oferta() {}
}
