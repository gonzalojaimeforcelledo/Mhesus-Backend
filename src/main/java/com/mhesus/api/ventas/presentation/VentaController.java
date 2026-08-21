package com.mhesus.api.ventas.presentation;

import com.mhesus.api.ventas.application.VentaRequest;
import com.mhesus.api.ventas.application.VentaService;
import com.mhesus.api.ventas.application.ResumenDiaResponse;
import com.mhesus.api.ventas.application.ResumenIgvResponse;
import com.mhesus.api.ventas.domain.Venta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ventas")
public class VentaController {
    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    private String usuarioId(HttpServletRequest req) {
        Object v = req.getAttribute("usuarioId");
        return v == null ? null : v.toString();
    }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return ventaService.listar().stream().map(this::aMapa).toList();
    }

    @GetMapping("/por-ot/{otId}")
    public ResponseEntity<Map<String, Object>> porOt(@PathVariable String otId) {
        return ventaService.porOt(otId)
                .map(v -> ResponseEntity.ok(aMapa(v)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody VentaRequest req, HttpServletRequest http) {
        try {
            Venta v = ventaService.crear(req, usuarioId(http));
            return ResponseEntity.ok(aMapa(v));
        } catch (VentaService.StockInsuficienteException e) {
            return ResponseEntity.status(409).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/anular")
    public Map<String, Object> anular(@PathVariable String id) {
        return aMapa(ventaService.anular(id));
    }

    @GetMapping("/resumen-dia")
    public ResumenDiaResponse resumenDia() {
        return ventaService.resumenDelDia();
    }

    @GetMapping("/reporte-mensual")
    public Map<String, Double> reporteMensual(@RequestParam(required = false) Integer anio) {
        int a = anio == null ? java.time.Year.now().getValue() : anio;
        return ventaService.ventasPorMes(a);
    }

    /** IGV a pagar del mes (ventas menos compras) — para el semáforo rojo/verde de Administración. */
    @GetMapping("/igv-mensual")
    public ResumenIgvResponse igvMensual(@RequestParam(required = false) Integer anio, @RequestParam(required = false) Integer mes) {
        var hoy = java.time.LocalDate.now();
        int a = anio == null ? hoy.getYear() : anio;
        int m = mes == null ? hoy.getMonthValue() : mes;
        return ventaService.resumenIgvMensual(a, m);
    }

    private Map<String, Object> aMapa(Venta v) {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", v.id);
        m.put("tipo", v.tipo);
        m.put("serie", v.serie);
        m.put("numero", v.numero);
        m.put("otId", v.otId);
        m.put("clienteId", v.clienteId);
        m.put("clienteNombre", v.clienteNombre);
        m.put("clienteDocumento", v.clienteDocumento);
        m.put("items", ventaService.leerDetalle(v));
        m.put("subtotal", v.subtotal);
        m.put("igv", v.igv);
        m.put("total", v.total);
        m.put("estado", v.estado);
        m.put("creadoPor", v.creadoPor);
        m.put("creadoEn", v.creadoEn);
        return m;
    }
}
