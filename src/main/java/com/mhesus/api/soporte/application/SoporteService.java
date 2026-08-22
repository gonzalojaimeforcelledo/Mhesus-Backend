package com.mhesus.api.soporte.application;

import com.mhesus.api.asistencia.domain.RegistroAsistenciaRepository;
import com.mhesus.api.soporte.domain.Notificacion;
import com.mhesus.api.soporte.domain.RegistroAuditoria;
import com.mhesus.api.soporte.domain.NotificacionRepository;
import com.mhesus.api.soporte.domain.RegistroAuditoriaRepository;
import com.mhesus.api.shared.util.IdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class SoporteService {
    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");

    private final RegistroAuditoriaRepository auditoriaRepository;
    private final NotificacionRepository notificacionRepository;
    private final RegistroAsistenciaRepository asistenciaRepository;

    public SoporteService(
            RegistroAuditoriaRepository auditoriaRepository, NotificacionRepository notificacionRepository,
            RegistroAsistenciaRepository asistenciaRepository
    ) {
        this.auditoriaRepository = auditoriaRepository;
        this.notificacionRepository = notificacionRepository;
        this.asistenciaRepository = asistenciaRepository;
    }

    public void registrarAuditoria(String otId, String usuarioId, String accion, String estadoAnterior, String estadoNuevo) {
        RegistroAuditoria r = new RegistroAuditoria();
        r.id = IdGenerator.generar("aud");
        r.otId = otId;
        r.usuarioId = usuarioId;
        r.accion = accion;
        r.estadoAnterior = estadoAnterior;
        r.estadoNuevo = estadoNuevo;
        r.creadoEn = Instant.now().toString();
        auditoriaRepository.save(r);
    }

    public List<RegistroAuditoria> auditoriaCompleta() {
        return auditoriaRepository.findAllByOrderByCreadoEnDesc();
    }

    public Notificacion notificar(String usuarioId, String mensaje, String otId) {
        Notificacion n = new Notificacion(IdGenerator.generar("not"), usuarioId, mensaje, otId, false, Instant.now().toString());
        return notificacionRepository.save(n);
    }

    public List<Notificacion> notificacionesDe(String usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByCreadoEnDesc(usuarioId);
    }

    public void marcarLeida(String id) {
        notificacionRepository.findById(id).ifPresent(n -> { n.leida = true; notificacionRepository.save(n); });
    }

    public void marcarTodasLeidas(String usuarioId) {
        List<Notificacion> lista = notificacionRepository.findByUsuarioIdOrderByCreadoEnDesc(usuarioId);
        lista.forEach(n -> n.leida = true);
        notificacionRepository.saveAll(lista);
    }

    /**
     * Limpieza de registros antiguos (Administración → Sistema): borra solo
     * historial/logs que se acumulan con el tiempo y no son parte de la
     * información operativa del taller — asistencia, notificaciones y
     * auditoría más antiguas que el corte elegido. A propósito NO toca
     * clientes, motos, OT, ventas ni cotizaciones: esos son los datos de
     * negocio del taller y no se borran automáticamente.
     */
    @Transactional
    public LimpiezaResultado limpiarRegistrosAntiguos(int meses) {
        LocalDate corte = LocalDate.now(ZONA_PERU).minusMonths(meses);
        String corteFecha = corte.toString();
        String corteInstant = corte.atStartOfDay(ZONA_PERU).toInstant().toString();

        long asistencia = asistenciaRepository.deleteByFechaLessThan(corteFecha);
        long notificaciones = notificacionRepository.deleteByCreadoEnLessThan(corteInstant);
        long auditoria = auditoriaRepository.deleteByCreadoEnLessThan(corteInstant);

        return new LimpiezaResultado(asistencia, notificaciones, auditoria);
    }

    public record LimpiezaResultado(long asistenciaBorrada, long notificacionesBorradas, long auditoriaBorrada) {}
}
