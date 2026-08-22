package com.mhesus.api.asistencia.application;

import com.mhesus.api.asistencia.domain.RegistroAsistencia;
import com.mhesus.api.asistencia.domain.RegistroAsistenciaRepository;
import com.mhesus.api.shared.util.IdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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
 */
@Service
public class AsistenciaService {
    @Value("${mhesus.ips-permitidas:}")
    private String ipsPermitidasCsv;

    private final RegistroAsistenciaRepository repo;

    public AsistenciaService(RegistroAsistenciaRepository repo) {
        this.repo = repo;
    }

    private List<String> ipsPermitidas() {
        if (ipsPermitidasCsv == null || ipsPermitidasCsv.isBlank()) return List.of();
        return Arrays.stream(ipsPermitidasCsv.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    public boolean ipPermitida(String ip) {
        List<String> permitidas = ipsPermitidas();
        return !permitidas.isEmpty() && ip != null && permitidas.contains(ip);
    }

    private String hoy() {
        return LocalDate.now().toString();
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

    public record Resultado(boolean ok, String error, RegistroAsistencia registro) {}

    private String horaActual() {
        return LocalTime.now().withNano(0).toString();
    }

    public Resultado marcarLlegada(String usuarioId, String ip) {
        if (!ipPermitida(ip)) {
            return new Resultado(false, "Para marcar tu llegada debes estar conectado al WiFi de MHESUS o MHESUS 5G.", null);
        }
        RegistroAsistencia r = registroDeHoy(usuarioId);
        if (r.horaLlegada != null) return new Resultado(false, "Ya marcaste tu llegada de hoy.", r);
        r.horaLlegada = horaActual();
        return new Resultado(true, null, repo.save(r));
    }

    public Resultado marcarInicioAlmuerzo(String usuarioId) {
        RegistroAsistencia r = registroDeHoy(usuarioId);
        if (r.horaLlegada == null) return new Resultado(false, "Primero marca tu llegada.", r);
        if (r.horaInicioAlmuerzo != null) return new Resultado(false, "Ya marcaste el inicio de tu almuerzo.", r);
        r.horaInicioAlmuerzo = horaActual();
        return new Resultado(true, null, repo.save(r));
    }

    public Resultado marcarFinAlmuerzo(String usuarioId) {
        RegistroAsistencia r = registroDeHoy(usuarioId);
        if (r.horaInicioAlmuerzo == null) return new Resultado(false, "Primero marca el inicio de tu almuerzo.", r);
        if (r.horaFinAlmuerzo != null) return new Resultado(false, "Ya marcaste el fin de tu almuerzo.", r);
        r.horaFinAlmuerzo = horaActual();
        return new Resultado(true, null, repo.save(r));
    }

    public Resultado marcarSalida(String usuarioId, String ip) {
        if (!ipPermitida(ip)) {
            return new Resultado(false, "Para marcar tu salida debes estar conectado al WiFi de MHESUS o MHESUS 5G.", null);
        }
        RegistroAsistencia r = registroDeHoy(usuarioId);
        if (r.horaLlegada == null) return new Resultado(false, "Todavía no marcaste tu llegada de hoy.", r);
        if (r.horaSalida != null) return new Resultado(false, "Ya marcaste tu salida de hoy.", r);
        r.horaSalida = horaActual();
        return new Resultado(true, null, repo.save(r));
    }
}
