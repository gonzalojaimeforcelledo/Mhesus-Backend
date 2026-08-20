package com.mhesus.api.ot.presentation;

import com.mhesus.api.ot.application.AsignarRequest;
import com.mhesus.api.ot.application.DiagnosticoRequest;
import com.mhesus.api.ot.application.EstadoRequest;
import com.mhesus.api.ot.application.OtCrearRequest;
import com.mhesus.api.ot.application.OtService;
import com.mhesus.api.ot.domain.Diagnostico;
import com.mhesus.api.ot.domain.OrdenTrabajo;
import com.mhesus.api.shared.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ot")
public class OtController {
    private final OtService otService;

    public OtController(OtService otService) {
        this.otService = otService;
    }

    private String usuarioId(HttpServletRequest req) {
        Object v = req.getAttribute("usuarioId");
        return v == null ? null : v.toString();
    }

    @GetMapping
    public List<OrdenTrabajo> listar() {
        return otService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenTrabajo> porId(@PathVariable String id) {
        return otService.porId(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public OrdenTrabajo crear(@RequestBody OtCrearRequest req, HttpServletRequest http) {
        return otService.crear(req, usuarioId(http));
    }

    @PatchMapping("/{id}/asignar")
    public OrdenTrabajo asignar(@PathVariable String id, @RequestBody AsignarRequest req, HttpServletRequest http) {
        return otService.asignarMecanico(id, req.mecanicoId(), usuarioId(http));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable String id, @RequestBody EstadoRequest req, HttpServletRequest http) {
        var res = otService.cambiarEstado(id, req.estado(), usuarioId(http), Boolean.TRUE.equals(req.forzar()));
        if (!res.ok()) return ResponseEntity.badRequest().body(new ErrorResponse(res.error()));
        return ResponseEntity.ok(res.ot());
    }

    @PatchMapping("/{id}/avanzar")
    public ResponseEntity<?> avanzar(@PathVariable String id, HttpServletRequest http) {
        var res = otService.avanzarEstado(id, usuarioId(http));
        if (!res.ok()) return ResponseEntity.badRequest().body(new ErrorResponse(res.error()));
        return ResponseEntity.ok(res.ot());
    }

    @PatchMapping("/{id}/finalizar-servicio")
    public ResponseEntity<?> finalizarServicio(@PathVariable String id, HttpServletRequest http) {
        var res = otService.finalizarServicioYAvanzar(id, usuarioId(http));
        if (!res.ok()) return ResponseEntity.badRequest().body(new ErrorResponse(res.error()));
        return ResponseEntity.ok(res.ot());
    }

    @PatchMapping("/{id}/aprobar-calidad")
    public ResponseEntity<?> aprobarCalidad(@PathVariable String id, HttpServletRequest http) {
        var res = otService.aprobarControlCalidad(id, usuarioId(http));
        if (!res.ok()) return ResponseEntity.badRequest().body(new ErrorResponse(res.error()));
        return ResponseEntity.ok(res.ot());
    }

    @PostMapping("/{id}/diagnostico")
    public Diagnostico registrarDiagnostico(@PathVariable String id, @RequestBody DiagnosticoRequest req, HttpServletRequest http) {
        return otService.registrarDiagnostico(id, req, usuarioId(http));
    }

    @GetMapping("/{id}/diagnostico")
    public ResponseEntity<Diagnostico> diagnostico(@PathVariable String id) {
        return otService.diagnosticoDe(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
