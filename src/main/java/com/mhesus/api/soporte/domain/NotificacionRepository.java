package com.mhesus.api.soporte.domain;

import com.mhesus.api.soporte.domain.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, String> {
    List<Notificacion> findByUsuarioIdOrderByCreadoEnDesc(String usuarioId);
}
