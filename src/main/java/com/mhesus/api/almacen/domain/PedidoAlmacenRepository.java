package com.mhesus.api.almacen.domain;

import com.mhesus.api.almacen.domain.PedidoAlmacen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PedidoAlmacenRepository extends JpaRepository<PedidoAlmacen, String> {
    List<PedidoAlmacen> findByOtId(String otId);
}
