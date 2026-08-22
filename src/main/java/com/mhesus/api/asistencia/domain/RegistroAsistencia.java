package com.mhesus.api.asistencia.domain;

import jakarta.persistence.*;

/**
 * Marcado de asistencia diario de un usuario (Almacén, Recepción, Mecánico).
 * Llegada y salida solo se pueden registrar conectado a la red WiFi del
 * taller (se valida por IP pública del router — ver AsistenciaService); el
 * almuerzo no tiene esa restricción, ya que puede tomarse fuera del taller.
 */
@Entity
@Table(name = "registros_asistencia")
public class RegistroAsistencia {
    @Id
    public String id;

    @Column(name = "usuario_id", nullable = false)
    public String usuarioId;

    @Column(nullable = false, length = 10)
    public String fecha; // YYYY-MM-DD

    @Column(name = "hora_llegada", length = 8)
    public String horaLlegada; // HH:mm:ss

    @Column(name = "hora_inicio_almuerzo", length = 8)
    public String horaInicioAlmuerzo;

    @Column(name = "hora_fin_almuerzo", length = 8)
    public String horaFinAlmuerzo;

    @Column(name = "hora_salida", length = 8)
    public String horaSalida;

    @Column(name = "creado_en", nullable = false)
    public String creadoEn;

    public RegistroAsistencia() {}
}
