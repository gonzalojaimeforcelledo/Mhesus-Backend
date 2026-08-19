package com.mhesus.api.cotizacion.domain;

import com.mhesus.api.cotizacion.domain.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CotizacionRepository extends JpaRepository<Cotizacion, String> {
    Optional<Cotizacion> findByOtId(String otId);
}
