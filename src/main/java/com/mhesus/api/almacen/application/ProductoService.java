package com.mhesus.api.almacen.application;

import com.mhesus.api.almacen.application.ProductoRequest;
import com.mhesus.api.almacen.domain.MovimientoInventario;
import com.mhesus.api.almacen.domain.Producto;
import com.mhesus.api.almacen.domain.MovimientoInventarioRepository;
import com.mhesus.api.almacen.domain.ProductoRepository;
import com.mhesus.api.shared.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public ProductoService(ProductoRepository productoRepository, MovimientoInventarioRepository movimientoRepository) {
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    public Optional<Producto> porId(String id) {
        return productoRepository.findById(id);
    }

    public Optional<Producto> porCodigo(String codigo) {
        return productoRepository.findByCodigoIgnoreCase(codigo);
    }

    public List<Producto> stockBajo() {
        return productoRepository.findAll().stream().filter(p -> p.stockActual <= p.stockMinimo).toList();
    }

    public Producto crear(ProductoRequest req) {
        Producto p = new Producto();
        p.id = IdGenerator.generar("prod");
        aplicar(p, req);
        return productoRepository.save(p);
    }

    public Producto actualizar(String id, ProductoRequest req) {
        Producto p = productoRepository.findById(id).orElseThrow();
        aplicar(p, req);
        return productoRepository.save(p);
    }

    private void aplicar(Producto p, ProductoRequest req) {
        // Precio anterior: se guarda solo — si el producto ya existía y el precio
        // cambió, la app puede mostrar "antes S/X, ahora S/Y" sin que nadie tenga
        // que anotarlo a mano.
        if (p.precio > 0 && req.precio() != p.precio) {
            p.precioAnterior = p.precio;
        }
        p.codigo = req.codigo();
        p.codigoBarras = req.codigoBarras();
        p.nombre = req.nombre();
        p.categoria = req.categoria();
        p.precio = req.precio();
        p.stockActual = req.stockActual();
        p.stockMinimo = req.stockMinimo();
        p.lugar = req.lugar();
        p.descuentoMaximo = req.descuentoMaximo();
        p.marcaMoto = req.marcaMoto();
        p.modeloMoto = req.modeloMoto();
        p.submodeloMoto = req.submodeloMoto();
        p.anioDesde = req.anioDesde();
        p.anioHasta = req.anioHasta();
    }

    public void eliminar(String id) {
        productoRepository.deleteById(id);
    }

    public Producto ajustarStock(String id, int delta, String usuarioId) {
        Producto p = productoRepository.findById(id).orElseThrow();
        p.stockActual += delta;
        p = productoRepository.save(p);
        registrarMovimiento(id, "ajuste", delta, null, usuarioId, null);
        return p;
    }

    public void registrarMovimiento(String productoId, String tipo, int cantidad, String otId, String usuarioId, String nota) {
        MovimientoInventario m = new MovimientoInventario();
        m.id = IdGenerator.generar("mov");
        m.productoId = productoId;
        m.tipo = tipo;
        m.cantidad = cantidad;
        m.otId = otId;
        m.usuarioId = usuarioId;
        m.nota = nota;
        m.creadoEn = Instant.now().toString();
        movimientoRepository.save(m);
    }

    public List<MovimientoInventario> movimientos() {
        return movimientoRepository.findAll();
    }

    public record ResultadoUpsert(Producto producto, boolean creado) {}

    /** Crea el producto si el código no existe, o actualiza sus datos si ya existe (usado por importación desde Excel). */
    public ResultadoUpsert upsertPorCodigo(ProductoRequest req) {
        Optional<Producto> existente = productoRepository.findByCodigoIgnoreCase(req.codigo());
        if (existente.isPresent()) {
            Producto p = existente.get();
            aplicar(p, req);
            return new ResultadoUpsert(productoRepository.save(p), false);
        }
        return new ResultadoUpsert(crear(req), true);
    }

    public record IngresoStockItem(String codigo, int cantidad, String nota) {}
    public record ResultadoIngresoItem(String codigo, boolean encontrado, String nombre, Integer stockNuevo) {}

    /**
     * Ingreso de stock por lote (Excel): a diferencia de "Importar desde Excel"
     * (que reemplaza el stock actual por el valor del archivo), esto SUMA la
     * cantidad indicada al stock que el producto ya tenía — para cuando llega
     * mercadería nueva, no para recargar el catálogo completo. Cada fila queda
     * como un movimiento de inventario con la nota que se haya escrito
     * (ej. "Pedido ingresado", "Corrección de inventario").
     */
    public List<ResultadoIngresoItem> ingresoStockPorLote(List<IngresoStockItem> items, String usuarioId) {
        return items.stream().map(item -> {
            Optional<Producto> op = productoRepository.findByCodigoIgnoreCase(item.codigo());
            if (op.isEmpty()) return new ResultadoIngresoItem(item.codigo(), false, null, null);
            Producto p = op.get();
            p.stockActual += item.cantidad();
            productoRepository.save(p);
            registrarMovimiento(p.id, "ingreso", item.cantidad(), null, usuarioId, item.nota());
            return new ResultadoIngresoItem(item.codigo(), true, p.nombre, p.stockActual);
        }).toList();
    }
}
