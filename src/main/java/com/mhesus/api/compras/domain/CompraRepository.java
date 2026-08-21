package com.mhesus.api.compras.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, String> {
    List<Compra> findByFechaBetweenOrderByFechaDesc(String desde, String hasta);
}
