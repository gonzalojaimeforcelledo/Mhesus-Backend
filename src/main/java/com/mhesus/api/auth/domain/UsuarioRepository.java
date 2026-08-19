package com.mhesus.api.auth.domain;

import com.mhesus.api.auth.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByUsuario(String usuario);
}
