package com.mhesus.api.auth.application;

import com.mhesus.api.auth.domain.CodigoRecuperacion;
import com.mhesus.api.auth.domain.CodigoRecuperacionRepository;
import com.mhesus.api.auth.domain.IntentoLogin;
import com.mhesus.api.auth.domain.IntentoLoginRepository;
import com.mhesus.api.auth.domain.Usuario;
import com.mhesus.api.auth.domain.UsuarioRepository;
import com.mhesus.api.auth.infrastructure.JwtUtil;
import com.mhesus.api.shared.util.IdGenerator;
import com.mhesus.api.soporte.application.SoporteService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
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
    private static final long CODIGO_VENCE_MINUTOS = 15;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final IntentoLoginRepository intentoLoginRepository;
    private final CodigoRecuperacionRepository codigoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SoporteService soporteService;
    private final EmailService emailService;

    public AuthService(UsuarioRepository usuarioRepository, IntentoLoginRepository intentoLoginRepository,
                        CodigoRecuperacionRepository codigoRepository, PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil, SoporteService soporteService, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.intentoLoginRepository = intentoLoginRepository;
        this.codigoRepository = codigoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.soporteService = soporteService;
        this.emailService = emailService;
    }

    /**
     * "No me acuerdo la contraseña" — no reseteamos nada nosotros mismos (no hay
     * correo configurado), en vez de eso le avisamos a TODOS los administradores
     * por notificación interna, para que sean ellos quienes restablezcan la
     * contraseña desde Administración → Usuarios. No confirma si el usuario existe
     * o no (para no filtrar esa información a quien no está autenticado).
     */
    @Transactional
    public void solicitarRestablecimiento(String usuario) {
        String clave = clave(usuario);
        if (clave.isEmpty()) return;
        Optional<Usuario> u = usuarioRepository.findByUsuario(usuario);
        String nombreMostrado = u.map(x -> x.nombre + " (" + x.usuario + ")").orElse(usuario);
        String mensaje = "Solicitud de restablecer contraseña: " + nombreMostrado;

        usuarioRepository.findAll().stream()
                .filter(x -> "administracion".equals(x.rol) && x.activo)
                .forEach(admin -> soporteService.notificar(admin.id, mensaje, null));
    }

    /**
     * Recuperación por correo — solo para administradores (el resto usa
     * "avisar a un administrador"). Si el usuario no existe, no es admin, o
     * el correo no coincide con el que tiene registrado, no dice nada
     * distinto — siempre responde igual, para no filtrar qué cuentas existen.
     */
    @Transactional
    public void solicitarCodigoRecuperacion(String usuario, String email) {
        String clave = clave(usuario);
        if (clave.isEmpty() || email == null || email.isBlank()) return;
        Optional<Usuario> u = usuarioRepository.findByUsuario(usuario)
                .filter(x -> x.activo)
                .filter(x -> "administracion".equals(x.rol))
                .filter(x -> x.email != null && x.email.equalsIgnoreCase(email.trim()));
        if (u.isEmpty()) return;

        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
        CodigoRecuperacion c = new CodigoRecuperacion();
        c.id = IdGenerator.generar("cod");
        c.usuarioId = u.get().id;
        c.codigo = codigo;
        c.expiraEn = Instant.now().plus(CODIGO_VENCE_MINUTOS, ChronoUnit.MINUTES).toString();
        c.usado = false;
        c.creadoEn = Instant.now().toString();
        codigoRepository.save(c);

        emailService.enviarCodigoRecuperacion(u.get().email, u.get().nombre, codigo);
    }

    public record ResultadoCodigo(boolean ok, String error) {}

    /** Confirma el código recibido por correo y, si es válido y no venció, actualiza la contraseña. */
    @Transactional
    public ResultadoCodigo confirmarCodigoRecuperacion(String usuario, String codigo, String nuevaPassword) {
        Optional<Usuario> uOpt = usuarioRepository.findByUsuario(usuario);
        if (uOpt.isEmpty()) return new ResultadoCodigo(false, "Código inválido.");
        Usuario u = uOpt.get();

        var c = codigoRepository.findTopByUsuarioIdAndCodigoAndUsadoFalseOrderByCreadoEnDesc(u.id, codigo).orElse(null);
        if (c == null) return new ResultadoCodigo(false, "Código inválido.");
        if (Instant.parse(c.expiraEn).isBefore(Instant.now())) return new ResultadoCodigo(false, "El código venció, solicita uno nuevo.");
        if (nuevaPassword == null || nuevaPassword.trim().length() < 6) return new ResultadoCodigo(false, "La contraseña debe tener al menos 6 caracteres.");

        c.usado = true;
        codigoRepository.save(c);
        u.passwordHash = passwordEncoder.encode(nuevaPassword.trim());
        usuarioRepository.save(u);
        return new ResultadoCodigo(true, null);
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
