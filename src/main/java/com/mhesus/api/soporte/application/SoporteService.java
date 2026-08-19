package com.mhesus.api.soporte.application;

import com.mhesus.api.soporte.domain.Notificacion;
import com.mhesus.api.soporte.domain.RegistroAuditoria;
import com.mhesus.api.soporte.domain.NotificacionRepository;
import com.mhesus.api.soporte.domain.RegistroAuditoriaRepository;
import com.mhesus.api.shared.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SoporteService {
    private final RegistroAuditoriaRepository auditoriaRepository;
    private final NotificacionRepository notificacionRepository;

    public SoporteService(RegistroAuditoriaRepository auditoriaRepository, NotificacionRepository notificacionRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.notificacionRepository = notificacionRepository;
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
}
