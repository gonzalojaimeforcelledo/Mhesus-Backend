package com.mhesus.api.asistencia.application;

import com.mhesus.api.asistencia.domain.RegistroAsistencia;

/**
 * Vista de un registro de asistencia para el panel de Administración: incluye
 * el nombre y rol del usuario (el registro solo guarda el usuarioId).
 */
public record RegistroAsistenciaAdminDto(
        String id,
        String usuarioId,
        String nombreUsuario,
        String rolUsuario,
        String fecha,
        String horaLlegada,
        String horaInicioAlmuerzo,
        String horaFinAlmuerzo,
        String horaSalida
) {
    public static RegistroAsistenciaAdminDto de(RegistroAsistencia r, String nombreUsuario, String rolUsuario) {
        return new RegistroAsistenciaAdminDto(
                r.id, r.usuarioId, nombreUsuario, rolUsuario, r.fecha,
                r.horaLlegada, r.horaInicioAlmuerzo, r.horaFinAlmuerzo, r.horaSalida
        );
    }
}
