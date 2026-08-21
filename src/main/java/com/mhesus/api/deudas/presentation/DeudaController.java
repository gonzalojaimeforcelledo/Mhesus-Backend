package com.mhesus.api.deudas.presentation;

import com.mhesus.api.deudas.application.AbonoRequest;
import com.mhesus.api.deudas.application.DeudaRequest;
import com.mhesus.api.deudas.application.DeudaService;
import com.mhesus.api.deudas.domain.Deuda;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deudas")
public class DeudaController {
    private final DeudaService deudaService;

    public DeudaController(DeudaService deudaService) {
        this.deudaService = deudaService;
    }

    private String usuarioId(HttpServletRequest req) {
        Object v = req.getAttribute("usuarioId");
        return v == null ? null : v.toString();
    }

    @GetMapping
    public List<Deuda> listar(@RequestParam(required = false) String tipo) {
        return (tipo == null || tipo.isBlank()) ? deudaService.listar() : deudaService.porTipo(tipo);
    }

    @PostMapping
    public Deuda crear(@RequestBody DeudaRequest req, HttpServletRequest http) {
        return deudaService.crear(req, usuarioId(http));
    }

    @PatchMapping("/{id}/abonar")
    public Deuda abonar(@PathVariable String id, @RequestBody AbonoRequest req) {
        return deudaService.abonar(id, req.monto());
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        deudaService.eliminar(id);
    }
}
