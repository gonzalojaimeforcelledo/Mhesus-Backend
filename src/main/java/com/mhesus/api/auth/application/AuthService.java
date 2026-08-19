package com.mhesus.api.auth.application;

import com.mhesus.api.auth.domain.Usuario;
import com.mhesus.api.auth.domain.UsuarioRepository;
import com.mhesus.api.auth.infrastructure.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public record ResultadoLogin(String token, Usuario usuario) {}

    public Optional<ResultadoLogin> login(String usuario, String password) {
        return usuarioRepository.findByUsuario(usuario)
                .filter(u -> u.activo)
                .filter(u -> passwordEncoder.matches(password, u.passwordHash))
                .map(u -> new ResultadoLogin(jwtUtil.generarToken(u.id, u.usuario, u.rol), u));
    }
}
