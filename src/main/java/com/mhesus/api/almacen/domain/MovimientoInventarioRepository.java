package com.mhesus.api.almacen.domain;

import com.mhesus.api.almacen.domain.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, String> {
}
