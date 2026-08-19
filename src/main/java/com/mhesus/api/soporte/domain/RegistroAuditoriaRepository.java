package com.mhesus.api.soporte.domain;

import com.mhesus.api.soporte.domain.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, String> {
    List<RegistroAuditoria> findAllByOrderByCreadoEnDesc();
}
