package com.mhesus.api.auth.application;

import com.mhesus.api.auth.application.UsuarioCrearRequest;
import com.mhesus.api.auth.domain.Usuario;
import com.mhesus.api.auth.domain.UsuarioRepository;
import com.mhesus.api.shared.util.IdGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> porId(String id) {
        return usuarioRepository.findById(id);
    }

    public List<Usuario> mecanicosActivos() {
        return usuarioRepository.findAll().stream()
                .filter(u -> "mecanico".equals(u.rol) && u.activo)
                .toList();
    }

    public Usuario crear(UsuarioCrearRequest req) {
        Usuario u = new Usuario(
                IdGenerator.generar("user"), req.nombre(), req.usuario(),
                passwordEncoder.encode("demo1234"), req.rol(), true
        );
        return usuarioRepository.save(u);
    }

    public Usuario alternarActivo(String id) {
        Usuario u = usuarioRepository.findById(id).orElseThrow();
        u.activo = !u.activo;
        return usuarioRepository.save(u);
    }
}
