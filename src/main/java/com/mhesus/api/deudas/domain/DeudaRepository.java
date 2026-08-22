package com.mhesus.api.deudas.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeudaRepository extends JpaRepository<Deuda, String> {
    List<Deuda> findByTipoOrderByCreadoEnDesc(String tipo);
    List<Deuda> findByEstado(String estado);
}
