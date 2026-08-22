package com.mhesus.api.asistencia.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistroAsistenciaRepository extends JpaRepository<RegistroAsistencia, String> {
    Optional<RegistroAsistencia> findByUsuarioIdAndFecha(String usuarioId, String fecha);

    // fecha es texto en formato YYYY-MM-DD, así que el orden alfabético
    // coincide con el orden cronológico — no hace falta parsear a LocalDate.
    List<RegistroAsistencia> findByFechaBetweenOrderByFechaDescUsuarioIdAsc(String desde, String hasta);

    long deleteByFechaLessThan(String fecha);
}
