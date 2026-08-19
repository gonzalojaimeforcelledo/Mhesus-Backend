package com.mhesus.api.cotizacion.presentation;

import com.mhesus.api.cotizacion.application.CotizacionRequest;
import com.mhesus.api.cotizacion.domain.Cotizacion;
import com.mhesus.api.cotizacion.application.CotizacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class CotizacionController {
    private final CotizacionService cotizacionService;

    public CotizacionController(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    @GetMapping("/api/v1/cotizaciones")
    public List<Cotizacion> listar() {
        return cotizacionService.listar();
    }

    @GetMapping("/api/v1/ot/{otId}/cotizacion")
    public ResponseEntity<?> deOt(@PathVariable String otId) {
        return cotizacionService.deOt(otId)
                .<ResponseEntity<?>>map(c -> ResponseEntity.ok(Map.of(
                        "id", c.id, "otId", c.otId, "detalle", cotizacionService.leerDetalle(c),
                        "montoTotal", c.montoTotal, "autorizado", c.autorizado, "autorizadoEn", c.autorizadoEn == null ? "" : c.autorizadoEn
                )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/api/v1/ot/{otId}/cotizacion")
    public Cotizacion generar(@PathVariable String otId, @RequestBody CotizacionRequest req) {
        return cotizacionService.generar(otId, req.detalle());
    }

    @PatchMapping("/api/v1/cotizaciones/{id}/autorizar")
    public Cotizacion autorizar(@PathVariable String id) {
        return cotizacionService.autorizar(id);
    }
}
