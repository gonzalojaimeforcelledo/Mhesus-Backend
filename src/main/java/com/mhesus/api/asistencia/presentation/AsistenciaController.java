package com.mhesus.api.asistencia.presentation;

import com.mhesus.api.asistencia.application.AsistenciaService;
import com.mhesus.api.asistencia.application.RegistroAsistenciaAdminDto;
import com.mhesus.api.asistencia.domain.RegistroAsistencia;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/asistencia")
public class AsistenciaController {
    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    private String usuarioId(HttpServletRequest req) {
        Object v = req.getAttribute("usuarioId");
        return v == null ? null : v.toString();
    }

    /**
     * IP real del que hace la petición. Railway (y la mayoría de hosting) pone
     * la app detrás de un proxy, así que request.getRemoteAddr() devolvería la
     * IP interna del proxy, no la del cliente — hay que leerla del header
     * X-Forwarded-For, que trae la cadena de IPs por las que pasó la petición
     * (la primera es la del cliente real).
     */
    private String ipCliente(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    @GetMapping("/hoy")
    public ResponseEntity<RegistroAsistencia> miAsistenciaDeHoy(HttpServletRequest http) {
        RegistroAsistencia r = asistenciaService.miAsistenciaDeHoy(usuarioId(http));
        return r == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(r);
    }

    /**
     * Listado de asistencia de todo el personal para el panel de
     * Administración. Filtros opcionales por rango de fecha (YYYY-MM-DD);
     * sin filtros, trae el mes calendario actual.
     */
    @GetMapping("/admin")
    public ResponseEntity<List<RegistroAsistenciaAdminDto>> listarParaAdmin(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ) {
        return ResponseEntity.ok(asistenciaService.listarParaAdmin(desde, hasta));
    }

    @PostMapping("/llegada")
    public ResponseEntity<?> marcarLlegada(HttpServletRequest http) {
        var res = asistenciaService.marcarLlegada(usuarioId(http), ipCliente(http));
        if (!res.ok()) return ResponseEntity.status(403).body(Map.of("mensaje", res.error()));
        return ResponseEntity.ok(res.registro());
    }

    @PostMapping("/almuerzo/inicio")
    public ResponseEntity<?> marcarInicioAlmuerzo(HttpServletRequest http) {
        var res = asistenciaService.marcarInicioAlmuerzo(usuarioId(http));
        if (!res.ok()) return ResponseEntity.status(400).body(Map.of("mensaje", res.error()));
        return ResponseEntity.ok(res.registro());
    }

    @PostMapping("/almuerzo/fin")
    public ResponseEntity<?> marcarFinAlmuerzo(HttpServletRequest http) {
        var res = asistenciaService.marcarFinAlmuerzo(usuarioId(http));
        if (!res.ok()) return ResponseEntity.status(400).body(Map.of("mensaje", res.error()));
        return ResponseEntity.ok(res.registro());
    }

    @PostMapping("/salida")
    public ResponseEntity<?> marcarSalida(HttpServletRequest http) {
        var res = asistenciaService.marcarSalida(usuarioId(http), ipCliente(http));
        if (!res.ok()) return ResponseEntity.status(403).body(Map.of("mensaje", res.error()));
        return ResponseEntity.ok(res.registro());
    }
}
