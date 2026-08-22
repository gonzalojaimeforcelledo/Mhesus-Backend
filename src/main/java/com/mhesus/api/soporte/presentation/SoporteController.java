package com.mhesus.api.soporte.presentation;

import com.mhesus.api.soporte.domain.Notificacion;
import com.mhesus.api.soporte.domain.RegistroAuditoria;
import com.mhesus.api.ot.domain.Diagnostico;
import com.mhesus.api.almacen.domain.PedidoDetalle;
import com.mhesus.api.ot.domain.DiagnosticoRepository;
import com.mhesus.api.almacen.domain.PedidoDetalleRepository;
import com.mhesus.api.soporte.application.SoporteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SoporteController {
    private final SoporteService soporteService;
    private final DiagnosticoRepository diagnosticoRepository;
    private final PedidoDetalleRepository pedidoDetalleRepository;

    public SoporteController(SoporteService soporteService, DiagnosticoRepository diagnosticoRepository, PedidoDetalleRepository pedidoDetalleRepository) {
        this.soporteService = soporteService;
        this.diagnosticoRepository = diagnosticoRepository;
        this.pedidoDetalleRepository = pedidoDetalleRepository;
    }

    private String usuarioId(HttpServletRequest req) {
        Object v = req.getAttribute("usuarioId");
        return v == null ? null : v.toString();
    }

    @GetMapping("/auditoria")
    public List<RegistroAuditoria> auditoria() {
        return soporteService.auditoriaCompleta();
    }

    /** Todos los diagnósticos (bulk) — usado por el frontend para poblar su estado reactivo local sin pedir uno por uno. */
    @GetMapping("/diagnosticos")
    public List<Diagnostico> todosLosDiagnosticos() {
        return diagnosticoRepository.findAll();
    }

    /** Todo el detalle de pedidos (bulk), mismo motivo que el endpoint anterior. */
    @GetMapping("/pedido-detalle")
    public List<PedidoDetalle> todoElDetalleDePedidos() {
        return pedidoDetalleRepository.findAll();
    }

    @GetMapping("/notificaciones/mias")
    public List<Notificacion> misNotificaciones(HttpServletRequest http) {
        return soporteService.notificacionesDe(usuarioId(http));
    }

    @PatchMapping("/notificaciones/{id}/leida")
    public void marcarLeida(@PathVariable String id) {
        soporteService.marcarLeida(id);
    }

    @PatchMapping("/notificaciones/leer-todas")
    public void marcarTodasLeidas(HttpServletRequest http) {
        soporteService.marcarTodasLeidas(usuarioId(http));
    }

    /**
     * Borra asistencia, notificaciones y auditoría más antiguas que el corte
     * elegido (6 o 12 meses). No toca clientes, OT, ventas ni cotizaciones.
     */
    @DeleteMapping("/sistema/limpieza")
    public SoporteService.LimpiezaResultado limpiarRegistrosAntiguos(@RequestParam int meses) {
        int mesesValido = (meses == 12) ? 12 : 6; // cualquier valor que no sea 12 se trata como el corte más conservador (6 meses)
        return soporteService.limpiarRegistrosAntiguos(mesesValido);
    }
}
