package com.mhesus.api.tareas.domain;

import jakarta.persistence.*;

/**
 * Nota / recordatorio / tarea asignada, mostrada en el Calendario de
 * Recepción, Almacén y Administración. tipo "recordatorio_moto" + motoId se
 * usa para avisar cuándo le toca a una moto su siguiente atención.
 */
@Entity
@Table(name = "tareas")
public class Tarea {
    @Id
    public String id;

    @Column(nullable = false, length = 200)
    public String titulo;

    @Column(length = 2000)
    public String descripcion;

    /** Fecha en formato ISO (YYYY-MM-DD) — a qué día del calendario pertenece. */
    @Column(nullable = false, length = 10)
    public String fecha;

    /** Hora opcional en formato HH:mm. */
    @Column(length = 5)
    public String hora;

    /** "nota" | "recordatorio" | "recordatorio_moto" | "tarea_asignada" */
    @Column(nullable = false, length = 30)
    public String tipo;

    /** Si es un recordatorio de "próxima atención", a qué moto se refiere. */
    @Column(name = "moto_id")
    public String motoId;

    @Column(name = "creado_por", nullable = false)
    public String creadoPor;

    /** Si el administrador asignó esta tarea a otro usuario, quién debe hacerla. Null = nota personal de quien la creó. */
    @Column(name = "asignado_a")
    public String asignadoA;

    @Column(nullable = false)
    public boolean completada;

    @Column(name = "creado_en", nullable = false)
    public String creadoEn;

    public Tarea() {}
}
