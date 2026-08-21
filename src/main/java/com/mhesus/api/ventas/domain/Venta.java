package com.mhesus.api.ventas.domain;

import jakarta.persistence.*;

/**
 * Un comprobante de venta interno (boleta, factura, nota de crédito/débito,
 * proforma o guía de remisión). MHESUS calcula todo y genera el PDF, pero
 * NO transmite a SUNAT por sí solo — eso requiere un operador autorizado
 * (OSE/PSE) con credenciales reales, ver README del backend para el punto
 * de integración dejado listo para conectar.
 */
@Entity
@Table(name = "ventas")
public class Venta {
    @Id
    public String id;

    /** FACTURA | BOLETA | NOTA_CREDITO | NOTA_DEBITO | PROFORMA | GUIA_REMISION */
    @Column(nullable = false, length = 20)
    public String tipo;

    @Column(nullable = false, length = 10)
    public String serie;

    @Column(nullable = false)
    public int numero;

    /** Si esta venta viene de una OT (repuestos que pidió el mecánico y aprobó el cliente). */
    @Column(name = "ot_id")
    public String otId;

    @Column(name = "cliente_id")
    public String clienteId;

    @Column(name = "cliente_nombre", length = 200)
    public String clienteNombre;

    @Column(name = "cliente_documento", length = 20)
    public String clienteDocumento;

    /** Detalle de ítems {descripcion, cantidad, precioUnitario} serializado como JSON. */
    @Column(name = "detalle_json", nullable = false, columnDefinition = "TEXT")
    public String detalleJson;

    @Column(nullable = false, columnDefinition = "NUMERIC(10,2)")
    public double subtotal;

    @Column(nullable = false, columnDefinition = "NUMERIC(10,2)")
    public double igv;

    @Column(nullable = false, columnDefinition = "NUMERIC(10,2)")
    public double total;

    /** EMITIDA | ANULADA */
    @Column(nullable = false, length = 20)
    public String estado;

    @Column(name = "creado_por", nullable = false)
    public String creadoPor;

    @Column(name = "creado_en", nullable = false)
    public String creadoEn;

    public Venta() {}
}
