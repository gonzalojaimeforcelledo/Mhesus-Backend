package com.mhesus.api.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoRecuperacionRepository extends JpaRepository<CodigoRecuperacion, String> {
    Optional<CodigoRecuperacion> findTopByUsuarioIdAndCodigoAndUsadoFalseOrderByCreadoEnDesc(String usuarioId, String codigo);
}
