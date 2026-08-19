package com.mhesus.api.almacen.domain;

import com.mhesus.api.almacen.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, String> {
    Optional<Producto> findByCodigoIgnoreCase(String codigo);
}
