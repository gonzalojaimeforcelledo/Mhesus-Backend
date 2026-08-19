package com.mhesus.api.clientes.domain;

import com.mhesus.api.clientes.domain.Motocicleta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MotocicletaRepository extends JpaRepository<Motocicleta, String> {
    List<Motocicleta> findByClienteId(String clienteId);
    Optional<Motocicleta> findByPlacaIgnoreCase(String placa);
    List<Motocicleta> findByPlacaContainingIgnoreCase(String placa);
}
