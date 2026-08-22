package com.mhesus.api.almacen.presentation;

import com.mhesus.api.almacen.application.AjusteStockRequest;
import com.mhesus.api.almacen.application.ProductoRequest;
import com.mhesus.api.almacen.domain.MovimientoInventario;
import com.mhesus.api.almacen.domain.Producto;
import com.mhesus.api.almacen.application.ProductoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    private String usuarioId(HttpServletRequest req) {
        Object v = req.getAttribute("usuarioId");
        return v == null ? null : v.toString();
    }

    @GetMapping
    public List<Producto> listar() {
        return productoService.listar();
    }

    @GetMapping("/stock-bajo")
    public List<Producto> stockBajo() {
        return productoService.stockBajo();
    }

    @GetMapping("/movimientos")
    public List<MovimientoInventario> movimientos() {
        return productoService.movimientos();
    }

    @PostMapping
    public Producto crear(@RequestBody ProductoRequest req) {
        return productoService.crear(req);
    }

    @PostMapping("/importar")
    public List<ProductoService.ResultadoUpsert> importar(@RequestBody List<ProductoRequest> filas) {
        return filas.stream().map(productoService::upsertPorCodigo).toList();
    }

    /** Ingreso de stock por lote (Excel): suma cantidad al stock existente, no lo reemplaza. Cada fila queda como movimiento con su nota. */
    @PostMapping("/ingreso-stock")
    public List<ProductoService.ResultadoIngresoItem> ingresoStock(@RequestBody List<ProductoService.IngresoStockItem> items, HttpServletRequest http) {
        return productoService.ingresoStockPorLote(items, usuarioId(http));
    }

    @PatchMapping("/{id}")
    public Producto actualizar(@PathVariable String id, @RequestBody ProductoRequest req) {
        return productoService.actualizar(id, req);
    }

    @PatchMapping("/{id}/ajustar-stock")
    public Producto ajustarStock(@PathVariable String id, @RequestBody AjusteStockRequest req, HttpServletRequest http) {
        return productoService.ajustarStock(id, req.delta(), usuarioId(http));
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        productoService.eliminar(id);
    }
}
