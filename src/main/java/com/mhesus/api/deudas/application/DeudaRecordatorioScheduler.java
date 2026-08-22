package com.mhesus.api.deudas.application;

import com.mhesus.api.auth.application.EmailService;
import com.mhesus.api.auth.domain.Usuario;
import com.mhesus.api.auth.domain.UsuarioRepository;
import com.mhesus.api.deudas.domain.Deuda;
import com.mhesus.api.deudas.domain.DeudaRepository;
import com.mhesus.api.soporte.application.SoporteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Recordatorio mensual de deudas "por cobrar" pendientes: una vez al mes,
 * avisa a Administración (notificación en el sistema + correo) por cada
 * deuda que sigue pendiente, indicando desde cuándo se otorgó y cuándo
 * vence. No se repite más de una vez por mes por deuda (ver
 * ultimaNotificacionMes).
 */
@Component
public class DeudaRecordatorioScheduler {
    private static final Logger log = LoggerFactory.getLogger(DeudaRecordatorioScheduler.class);
    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final DateTimeFormatter YYYY_MM = DateTimeFormatter.ofPattern("yyyy-MM");

    private final DeudaRepository deudaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SoporteService soporteService;
    private final EmailService emailService;

    public DeudaRecordatorioScheduler(
            DeudaRepository deudaRepository, UsuarioRepository usuarioRepository,
            SoporteService soporteService, EmailService emailService
    ) {
        this.deudaRepository = deudaRepository;
        this.usuarioRepository = usuarioRepository;
        this.soporteService = soporteService;
        this.emailService = emailService;
    }

    /** Corre todos los días a las 8:00 am hora de Perú; cada deuda solo se notifica una vez por mes calendario. */
    @Scheduled(cron = "0 0 8 * * *", zone = "America/Lima")
    public void enviarRecordatoriosMensuales() {
        String mesActual = LocalDate.now(ZONA_PERU).format(YYYY_MM);
        List<Deuda> pendientes = deudaRepository.findByEstado("PENDIENTE").stream()
                .filter(d -> "POR_COBRAR".equals(d.tipo))
                .filter(d -> !mesActual.equals(d.ultimaNotificacionMes))
                .toList();
        if (pendientes.isEmpty()) return;

        List<Usuario> administradores = usuarioRepository.findAll().stream()
                .filter(u -> "administracion".equalsIgnoreCase(u.rol) && u.activo)
                .toList();

        for (Deuda d : pendientes) {
            String mensaje = "Recordatorio: " + d.nombre + " aún debe S/ " + String.format("%.2f", d.montoPendiente)
                    + " (deuda desde " + (d.fechaInicio != null ? d.fechaInicio : "—")
                    + (d.fechaVencimiento != null ? ", vence " + d.fechaVencimiento : "") + ").";

            for (Usuario admin : administradores) {
                soporteService.notificar(admin.id, mensaje, null);
                if (admin.email != null && !admin.email.isBlank()) {
                    try {
                        emailService.enviarRecordatorioDeuda(admin.email, d.nombre, d.montoPendiente, d.fechaInicio, d.fechaVencimiento);
                    } catch (Exception e) {
                        log.warn("No se pudo enviar el correo de recordatorio de deuda: {}", e.getMessage());
                    }
                }
            }

            d.ultimaNotificacionMes = mesActual;
            deudaRepository.save(d);
        }
    }
}
