package com.mhesus.api.compras.domain;

import jakarta.persistence.*;

/**
 * Registro simple de una compra a proveedor (factura recibida), para poder
 * calcular el IGV a pagar del mes: IGV de ventas (débito fiscal) menos IGV
 * de compras (crédito fiscal). No es un módulo completo de proveedores —
 * solo lo necesario para el cálculo de IGV en Administración.
 */
@Entity
@Table(name = "compras")
public class Compra {
    @Id
    public String id;

    @Column(nullable = false, length = 200)
    public String proveedor;

    @Column(length = 500)
    public String descripcion;

    /** Número de factura/comprobante del proveedor, si lo tienen a mano. */
    @Column(name = "numero_comprobante", length = 50)
    public String numeroComprobante;

    @Column(name = "monto_total", nullable = false, columnDefinition = "NUMERIC(10,2)")
    public double montoTotal; // incluye IGV

    @Column(nullable = false, columnDefinition = "NUMERIC(10,2)")
    public double igv; // calculado: montoTotal - montoTotal/1.18

    /** Fecha en formato ISO (YYYY-MM-DD) — a qué mes de IGV pertenece. */
    @Column(nullable = false, length = 10)
    public String fecha;

    @Column(name = "creado_por", nullable = false)
    public String creadoPor;

    @Column(name = "creado_en", nullable = false)
    public String creadoEn;

    public Compra() {}
}
