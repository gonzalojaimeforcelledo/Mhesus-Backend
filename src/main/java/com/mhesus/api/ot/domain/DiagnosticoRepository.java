package com.mhesus.api.ot.domain;

import com.mhesus.api.ot.domain.Diagnostico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DiagnosticoRepository extends JpaRepository<Diagnostico, String> {
    Optional<Diagnostico> findByOtId(String otId);
}
