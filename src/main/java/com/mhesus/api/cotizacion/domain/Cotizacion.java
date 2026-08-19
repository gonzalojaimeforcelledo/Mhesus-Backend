package com.mhesus.api.cotizacion.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "cotizaciones")
public class Cotizacion {
    @Id
    public String id;

    @Column(name = "ot_id", nullable = false, unique = true)
    public String otId;

    /** Lista de ítems {descripcion, cantidad, precioUnitario} serializada como JSON. */
    @Column(name = "detalle_json", nullable = false, columnDefinition = "TEXT")
    public String detalleJson;

    @Column(name = "monto_total", nullable = false, columnDefinition = "NUMERIC(10,2)")
    public double montoTotal;

    @Column(nullable = false)
    public boolean autorizado;

    @Column(name = "autorizado_en")
    public String autorizadoEn;

    public Cotizacion() {}
}
