package com.mhesus.api.ventas.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, String> {
    List<Venta> findByCreadoEnBetweenOrderByCreadoEnDesc(String desde, String hasta);
    List<Venta> findAllByOrderByCreadoEnDesc();

    /** Para calcular el siguiente número correlativo de una serie+tipo. */
    long countByTipoAndSerie(String tipo, String serie);

    Optional<Venta> findByOtId(String otId);
}
