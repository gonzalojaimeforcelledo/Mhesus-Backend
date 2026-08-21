package com.mhesus.api.compras.presentation;

import com.mhesus.api.compras.application.CompraRequest;
import com.mhesus.api.compras.application.CompraService;
import com.mhesus.api.compras.domain.Compra;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/compras")
public class CompraController {
    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    private String usuarioId(HttpServletRequest req) {
        Object v = req.getAttribute("usuarioId");
        return v == null ? null : v.toString();
    }

    @GetMapping
    public List<Compra> listar() {
        return compraService.listar();
    }

    @PostMapping
    public Compra crear(@RequestBody CompraRequest req, HttpServletRequest http) {
        return compraService.crear(req, usuarioId(http));
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        compraService.eliminar(id);
    }
}
