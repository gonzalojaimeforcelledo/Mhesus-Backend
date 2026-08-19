package com.mhesus.api.ot.domain;

import com.mhesus.api.ot.domain.OrdenTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, String> {
    List<OrdenTrabajo> findByMotoIdOrderByCreadoEnDesc(String motoId);
    long countByNumeroOTContaining(String anio);
}
