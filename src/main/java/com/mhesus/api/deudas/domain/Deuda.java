package com.mhesus.api.deudas.domain;

import jakarta.persistence.*;

/**
 * Deuda genérica para el módulo financiero de Administración: puede ser
 * "por cobrar" (lo que un cliente le debe al taller, ej. venta a crédito)
 * o "de banco" (préstamo/deuda del taller con una entidad financiera).
 */
@Entity
@Table(name = "deudas")
public class Deuda {
    @Id
    public String id;

    /** POR_COBRAR | BANCO */
    @Column(nullable = false, length = 20)
    public String tipo;

    /** Nombre del cliente que debe (POR_COBRAR) o del banco/entidad (BANCO). */
    @Column(nullable = false, length = 200)
    public String nombre;

    @Column(length = 500)
    public String descripcion;

    /** Solo aplica a POR_COBRAR — referencia opcional al cliente registrado. */
    @Column(name = "cliente_id")
    public String clienteId;

    @Column(name = "monto_original", nullable = false, columnDefinition = "NUMERIC(10,2)")
    public double montoOriginal;

    @Column(name = "monto_pendiente", nullable = false, columnDefinition = "NUMERIC(10,2)")
    public double montoPendiente;

    @Column(name = "fecha_vencimiento")
    public String fechaVencimiento; // YYYY-MM-DD, opcional

    /** Solo aplica a POR_COBRAR — datos de contacto y garantía dejada, opcionales. */
    @Column(length = 20)
    public String celular;

    @Column(length = 300)
    public String direccion;

    /** Qué dejó como garantía (ej. "DNI", "tarjeta de propiedad"), opcional. */
    @Column(length = 300)
    public String garantia;

    /** Fecha en que se otorgó/solicitó la deuda (puede diferir de creadoEn si se registra en retrospectiva). */
    @Column(name = "fecha_inicio")
    public String fechaInicio; // YYYY-MM-DD

    /** Último mes (YYYY-MM) en que se envió el recordatorio automático — evita notificar más de una vez por mes. */
    @Column(name = "ultima_notificacion_mes", length = 7)
    public String ultimaNotificacionMes;

    /** PENDIENTE | PAGADA */
    @Column(nullable = false, length = 20)
    public String estado;

    @Column(name = "creado_por", nullable = false)
    public String creadoPor;

    @Column(name = "creado_en", nullable = false)
    public String creadoEn;

    public Deuda() {}
}
