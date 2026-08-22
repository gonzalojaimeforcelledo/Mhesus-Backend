package com.mhesus.api.asistencia.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistroAsistenciaRepository extends JpaRepository<RegistroAsistencia, String> {
    Optional<RegistroAsistencia> findByUsuarioIdAndFecha(String usuarioId, String fecha);
}
