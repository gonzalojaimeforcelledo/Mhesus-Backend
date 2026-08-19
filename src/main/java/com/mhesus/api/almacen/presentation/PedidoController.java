package com.mhesus.api.almacen.presentation;

import com.mhesus.api.almacen.application.DespacharRequest;
import com.mhesus.api.shared.dto.ErrorResponse;
import com.mhesus.api.almacen.application.PedidoRequest;
import com.mhesus.api.almacen.domain.PedidoAlmacen;
import com.mhesus.api.almacen.domain.PedidoDetalle;
import com.mhesus.api.almacen.application.PedidoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PedidoController {
    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    private String usuarioId(HttpServletRequest req) {
        Object v = req.getAttribute("usuarioId");
        return v == null ? null : v.toString();
    }

    @GetMapping("/api/v1/pedidos")
    public List<PedidoAlmacen> todos() {
        return pedidoService.todos();
    }

    @GetMapping("/api/v1/ot/{otId}/pedidos")
    public List<PedidoAlmacen> deOt(@PathVariable String otId) {
        return pedidoService.deOt(otId);
    }

    @PostMapping("/api/v1/ot/{otId}/pedidos")
    public PedidoAlmacen generar(@PathVariable String otId, @RequestBody PedidoRequest req, HttpServletRequest http) {
        return pedidoService.generar(otId, req.items(), usuarioId(http));
    }

    @GetMapping("/api/v1/pedidos/{id}/detalle")
    public List<PedidoDetalle> detalle(@PathVariable String id) {
        return pedidoService.detalleDe(id);
    }

    @PatchMapping("/api/v1/pedidos/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable String id, HttpServletRequest http) {
        var res = pedidoService.aprobarParaAlmacen(id, usuarioId(http));
        if (!res.ok()) return ResponseEntity.badRequest().body(new ErrorResponse(res.error()));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/v1/ot/{otId}/pedidos/{id}/confirmar-y-enviar")
    public ResponseEntity<?> confirmarYEnviar(@PathVariable String otId, @PathVariable String id, HttpServletRequest http) {
        var res = pedidoService.confirmarAceptacionYEnviar(otId, id, usuarioId(http));
        if (!res.ok()) return ResponseEntity.badRequest().body(new ErrorResponse(res.error()));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/v1/pedidos/{id}/despachar")
    public ResponseEntity<?> despachar(@PathVariable String id, @RequestBody(required = false) DespacharRequest req, HttpServletRequest http) {
        String foto = req == null ? null : req.fotoDespacho();
        var res = pedidoService.despachar(id, usuarioId(http), foto);
        if (!res.ok()) return ResponseEntity.badRequest().body(new ErrorResponse(res.error()));
        return ResponseEntity.ok().build();
    }
}
