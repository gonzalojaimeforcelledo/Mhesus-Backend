package com.mhesus.api.auth.application;

import com.mhesus.api.auth.domain.IntentoLogin;
import com.mhesus.api.auth.domain.IntentoLoginRepository;
import com.mhesus.api.auth.domain.Usuario;
import com.mhesus.api.auth.domain.UsuarioRepository;
import com.mhesus.api.auth.infrastructure.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Login + control de intentos fallidos por usuario (3 intentos, bloqueo de
 * 5 minutos), persistido en la tabla intentos_login — a propósito en base de
 * datos y no en memoria, para que el bloqueo siga vigente aunque el backend
 * se reinicie (Railway redeploy, caída, etc.) y no se pueda saltar limpiando
 * el localStorage del navegador.
 */
@Service
public class AuthService {
    private static final int MAX_INTENTOS = 3;
    private static final long BLOQUEO_MINUTOS = 5;

    private final UsuarioRepository usuarioRepository;
    private final IntentoLoginRepository intentoLoginRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioRepository usuarioRepository, IntentoLoginRepository intentoLoginRepository,
                        PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.intentoLoginRepository = intentoLoginRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public record ResultadoLogin(boolean ok, String token, Usuario usuario, String error, Long bloqueadoHastaMillis) {
        static ResultadoLogin exito(String token, Usuario usuario) {
            return new ResultadoLogin(true, token, usuario, null, null);
        }
        static ResultadoLogin error(String mensaje, Long bloqueadoHastaMillis) {
            return new ResultadoLogin(false, null, null, mensaje, bloqueadoHastaMillis);
        }
    }

    private static String clave(String usuario) {
        return usuario == null ? "" : usuario.trim().toLowerCase();
    }

    /** Milisegundos epoch hasta los que sigue bloqueado ese usuario, o null si puede intentar. Ya limpia bloqueos vencidos. */
    @Transactional
    public Long estadoBloqueo(String usuario) {
        String clave = clave(usuario);
        if (clave.isEmpty()) return null;
        var intento = intentoLoginRepository.findById(clave).orElse(null);
        if (intento == null || intento.bloqueadoHasta == null) return null;
        Instant hasta = Instant.parse(intento.bloqueadoHasta);
        if (!hasta.isAfter(Instant.now())) {
            intentoLoginRepository.deleteById(clave);
            return null;
        }
        return hasta.toEpochMilli();
    }

    @Transactional
    public ResultadoLogin login(String usuario, String password) {
        String clave = clave(usuario);
        var intento = intentoLoginRepository.findById(clave).orElse(null);

        // Si ya está bloqueado y el bloqueo sigue vigente, corta acá sin ni siquiera mirar la contraseña.
        if (intento != null && intento.bloqueadoHasta != null) {
            Instant hasta = Instant.parse(intento.bloqueadoHasta);
            if (hasta.isAfter(Instant.now())) {
                long minutos = Math.max(1, ChronoUnit.MINUTES.between(Instant.now(), hasta) + 1);
                return ResultadoLogin.error(
                    "Demasiados intentos fallidos. Vuelve a intentar en " + minutos + " minuto" + (minutos == 1 ? "" : "s") + ".",
                    hasta.toEpochMilli()
                );
            }
            // el bloqueo ya venció: lo limpiamos y dejamos intentar de nuevo
            intentoLoginRepository.deleteById(clave);
            intento = null;
        }

        Optional<Usuario> u = usuarioRepository.findByUsuario(usuario)
                .filter(x -> x.activo)
                .filter(x -> passwordEncoder.matches(password, x.passwordHash));

        if (u.isPresent()) {
            if (intento != null) intentoLoginRepository.deleteById(clave);
            String token = jwtUtil.generarToken(u.get().id, u.get().usuario, u.get().rol);
            return ResultadoLogin.exito(token, u.get());
        }

        // credenciales incorrectas: sumamos un intento
        int intentosPrevios = intento == null ? 0 : intento.intentos;
        int nuevosIntentos = intentosPrevios + 1;

        if (nuevosIntentos >= MAX_INTENTOS) {
            Instant hasta = Instant.now().plus(BLOQUEO_MINUTOS, ChronoUnit.MINUTES);
            IntentoLogin nuevo = new IntentoLogin();
            nuevo.usuario = clave;
            nuevo.intentos = 0;
            nuevo.bloqueadoHasta = hasta.toString();
            intentoLoginRepository.save(nuevo);
            return ResultadoLogin.error("Demasiados intentos fallidos. Acceso bloqueado por 5 minutos.", hasta.toEpochMilli());
        }

        IntentoLogin nuevo = new IntentoLogin();
        nuevo.usuario = clave;
        nuevo.intentos = nuevosIntentos;
        nuevo.bloqueadoHasta = null;
        intentoLoginRepository.save(nuevo);
        int restantes = MAX_INTENTOS - nuevosIntentos;
        return ResultadoLogin.error(
            "Usuario o contraseña incorrectos. Te queda" + (restantes == 1 ? "" : "n") + " " + restantes + " intento" + (restantes == 1 ? "" : "s") + ".",
            null
        );
    }
}
