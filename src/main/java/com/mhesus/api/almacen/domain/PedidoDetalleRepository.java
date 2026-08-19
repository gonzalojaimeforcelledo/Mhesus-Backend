package com.mhesus.api.almacen.domain;

import com.mhesus.api.almacen.domain.PedidoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalle, String> {
    List<PedidoDetalle> findByPedidoId(String pedidoId);
}
