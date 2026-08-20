package com.mhesus.api.clientes.domain;

import com.mhesus.api.clientes.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, String> {
    Optional<Cliente> findByDni(String dni);
    List<Cliente> findByDniStartingWith(String dniParcial);
}
