package com.mhesus.api.asistencia.application;

import com.mhesus.api.asistencia.domain.RegistroAsistencia;
import com.mhesus.api.asistencia.domain.RegistroAsistenciaRepository;
import com.mhesus.api.auth.application.EmailService;
import com.mhesus.api.auth.domain.Usuario;
import com.mhesus.api.auth.domain.UsuarioRepository;
import com.mhesus.api.shared.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

/**
 * Marcado de llegada/almuerzo/salida. Llegada y salida exigen que la IP
 * pública del que hace la petición esté en la lista de IPs permitidas del
 * WiFi del taller (MHESUS_IPS_PERMITIDAS, separadas por coma) — es la única
 * forma real de verificar "conectado al WiFi del taller" desde un sitio web,
 * ya que el navegador no puede leer el nombre de la red WiFi por seguridad.
 * Si la lista está vacía (no configurada todavía), se BLOQUEA todo por
 * seguridad en vez de dejar pasar cualquier IP.
 *
 * Cada marcado (llegada, almuerzo, salida) es independiente: se puede marcar
 * en cualquier momento del día, sin exigir que los anteriores ya estén
 * registrados (ej. se puede marcar "inicio de almuerzo" sin haber marcado
 * "llegada" antes). Lo único que se sigue validando es no duplicar un mismo
 * marcado el mismo día.
 */
@Service
public class AsistenciaService {
    private static final Logger log = LoggerFactory.getLogger(AsistenciaService.class);

    @Value("${mhesus.ips-permitidas:}")
    private String ipsPermitidasCsv;

    private final RegistroAsistenciaRepository repo;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    public AsistenciaService(RegistroAsistenciaRepository repo, UsuarioRepository usuarioRepository, EmailService emailService) {
        this.repo = repo;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }

    private List<String> ipsPermitidas() {
        if (ipsPermitidasCsv == null || ipsPermitidasCsv.isBlank()) return List.of();
        return Arrays.stream(ipsPermitidasCsv.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    public boolean ipPermitida(String ip) {
        List<String> permitidas = ipsPermitidas();
        return !permitidas.isEmpty() && ip != null && permitidas.contains(ip);
    }

    // El servidor (Railway) corre en UTC, pero el taller está en Perú
    // (UTC-5, sin horario de verano). Sin esto, la hora y hasta la fecha
    // registradas quedarían desfasadas respecto a la hora real del taller.
    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");

    private String hoy() {
        return LocalDate.now(ZONA_PERU).toString();
    }

    private RegistroAsistencia registroDeHoy(String usuarioId) {
        return repo.findByUsuarioIdAndFecha(usuarioId, hoy()).orElseGet(() -> {
            RegistroAsistencia r = new RegistroAsistencia();
            r.id = IdGenerator.generar("asis");
            r.usuarioId = usuarioId;
            r.fecha = hoy();
            r.creadoEn = Instant.now().toString();
            return r;
        });
    }

    public RegistroAsistencia miAsistenciaDeHoy(String usuarioId) {
        return repo.findByUsuarioIdAndFecha(usuarioId, hoy()).orElse(null);
    }

    /**
     * Listado de asistencia de todos los usuarios para el panel de
     * Administración, con nombre y rol ya resueltos. Si no se pasan fechas,
     * trae el mes calendario actual (hora de Perú).
     */
    public List<RegistroAsistenciaAdminDto> listarParaAdmin(String desde, String hasta) {
        String desdeFinal = (desde == null || desde.isBlank()) ? primerDiaDelMesActual() : desde;
        String hastaFinal = (hasta == null || hasta.isBlank()) ? hoy() : hasta;

        java.util.Map<String, Usuario> usuariosPorId = usuarioRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(u -> u.id, u -> u));

        return repo.findByFechaBetweenOrderByFechaDescUsuarioIdAsc(desdeFinal, hastaFinal).stream()
                .map(r -> {
                    Usuario u = usuariosPorId.get(r.usuarioId);
                    String nombre = u != null ? u.nombre : r.usuarioId;
                    String rol = u != null ? u.rol : "—";
                    return RegistroAsistenciaAdminDto.de(r, nombre, rol);
                })
                .toList();
    }

    private String primerDiaDelMesActual() {
        return LocalDate.now(ZONA_PERU).withDayOfMonth(1).toString();
    }

    public record Resultado(boolean ok, String error, RegistroAsistencia registro) {}

    private String horaActual() {
        return LocalTime.now(ZONA_PERU).withNano(0).toString();
    }

    private String mensajeIpNoPermitida(String ip) {
        // Se incluye la IP detectada en el mensaje para que, al configurar el
        // sistema por primera vez desde el taller, sea fácil ver qué IP hay
        // que agregar a MHESUS_IPS_PERMITIDAS sin tener que revisar logs.
        String detectada = (ip == null || ip.isBlank()) ? "desconocida" : ip;
        return "Para marcar debes estar conectado al WiFi de MHESUS o MHESUS 5G. (IP detectada: " + detectada + ")";
    }

    public Resultado marcarLlegada(String usuarioId, String ip) {
        if (!ipPermitida(ip)) {
            return new Resultado(false, mensajeIpNoPermitida(ip), null);
        }
        RegistroAsistencia r = registroDeHoy(usuarioId);
        if (r.horaLlegada != null) return new Resultado(false, "Ya marcaste tu llegada de hoy.", r);
        r.horaLlegada = horaActual();
        RegistroAsistencia guardado = repo.save(r);
        notificarAdministracion(usuarioId, "Llegada", r.horaLlegada, r.fecha);
        return new Resultado(true, null, guardado);
    }

    public Resultado marcarInicioAlmuerzo(String usuarioId) {
        RegistroAsistencia r = registroDeHoy(usuarioId);
        if (r.horaInicioAlmuerzo != null) return new Resultado(false, "Ya marcaste el inicio de tu almuerzo.", r);
        r.horaInicioAlmuerzo = horaActual();
        RegistroAsistencia guardado = repo.save(r);
        notificarAdministracion(usuarioId, "Inicio de almuerzo", r.horaInicioAlmuerzo, r.fecha);
        return new Resultado(true, null, guardado);
    }

    public Resultado marcarFinAlmuerzo(String usuarioId) {
        RegistroAsistencia r = registroDeHoy(usuarioId);
        if (r.horaFinAlmuerzo != null) return new Resultado(false, "Ya marcaste el fin de tu almuerzo.", r);
        r.horaFinAlmuerzo = horaActual();
        RegistroAsistencia guardado = repo.save(r);
        notificarAdministracion(usuarioId, "Fin de almuerzo", r.horaFinAlmuerzo, r.fecha);
        return new Resultado(true, null, guardado);
    }

    public Resultado marcarSalida(String usuarioId, String ip) {
        if (!ipPermitida(ip)) {
            return new Resultado(false, mensajeIpNoPermitida(ip), null);
        }
        RegistroAsistencia r = registroDeHoy(usuarioId);
        if (r.horaSalida != null) return new Resultado(false, "Ya marcaste tu salida de hoy.", r);
        r.horaSalida = horaActual();
        RegistroAsistencia guardado = repo.save(r);
        notificarAdministracion(usuarioId, "Salida", r.horaSalida, r.fecha);
        return new Resultado(true, null, guardado);
    }

    /**
     * Envía un correo a todos los usuarios con rol "administracion" que
     * tengan email configurado, avisando qué marcado se hizo, a qué hora y
     * qué fecha. No debe romper el marcado de asistencia si el envío falla
     * (ej. SMTP no configurado todavía) — por eso se atrapa cualquier error.
     */
    private void notificarAdministracion(String usuarioId, String tipoMarcado, String hora, String fecha) {
        try {
            Usuario quienMarco = usuarioRepository.findById(usuarioId).orElse(null);
            String nombre = quienMarco != null ? quienMarco.nombre : usuarioId;
            List<String> destinatarios = usuarioRepository.findAll().stream()
                    .filter(u -> "administracion".equalsIgnoreCase(u.rol))
                    .map(u -> u.email)
                    .filter(e -> e != null && !e.isBlank())
                    .toList();
            if (destinatarios.isEmpty()) return;
            for (String destino : destinatarios) {
                emailService.enviarReporteAsistencia(destino, nombre, tipoMarcado, fecha, hora);
            }
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo de reporte de asistencia: {}", e.getMessage());
        }
    }
}
