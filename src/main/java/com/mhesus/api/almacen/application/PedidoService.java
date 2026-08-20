package com.mhesus.api.almacen.application;

import com.mhesus.api.almacen.domain.PedidoAlmacen;
import com.mhesus.api.almacen.domain.PedidoAlmacenRepository;
import com.mhesus.api.almacen.domain.PedidoDetalle;
import com.mhesus.api.almacen.domain.PedidoDetalleRepository;
import com.mhesus.api.almacen.domain.Producto;
import com.mhesus.api.almacen.domain.ProductoRepository;
import com.mhesus.api.auth.domain.UsuarioRepository;
import com.mhesus.api.cotizacion.application.CotizacionService;
import com.mhesus.api.cotizacion.application.ItemCotizacionDto;
import com.mhesus.api.cotizacion.domain.Cotizacion;
import com.mhesus.api.ot.application.OtService;
import com.mhesus.api.ot.domain.OrdenTrabajo;
import com.mhesus.api.ot.domain.OrdenTrabajoRepository;
import com.mhesus.api.soporte.application.SoporteService;
import com.mhesus.api.shared.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {
    private final PedidoAlmacenRepository pedidoRepository;
    private final PedidoDetalleRepository detalleRepository;
    private final ProductoRepository productoRepository;
    private final OrdenTrabajoRepository otRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoService productoService;
    private final CotizacionService cotizacionService;
    private final SoporteService soporteService;
    private final OtService otService;

    public PedidoService(PedidoAlmacenRepository pedidoRepository, PedidoDetalleRepository detalleRepository,
                          ProductoRepository productoRepository, OrdenTrabajoRepository otRepository,
                          UsuarioRepository usuarioRepository, ProductoService productoService,
                          CotizacionService cotizacionService, SoporteService soporteService, OtService otService) {
        this.pedidoRepository = pedidoRepository;
        this.detalleRepository = detalleRepository;
        this.productoRepository = productoRepository;
        this.otRepository = otRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoService = productoService;
        this.cotizacionService = cotizacionService;
        this.soporteService = soporteService;
        this.otService = otService;
    }

    public List<PedidoAlmacen> deOt(String otId) {
        return pedidoRepository.findByOtId(otId);
    }

    public List<PedidoDetalle> detalleDe(String pedidoId) {
        return detalleRepository.findByPedidoId(pedidoId);
    }

    public List<PedidoAlmacen> todos() {
        return pedidoRepository.findAll();
    }

    public PedidoAlmacen generar(String otId, List<ItemPedido> items, String creadoPor) {
        PedidoAlmacen pedido = new PedidoAlmacen();
        pedido.id = IdGenerator.generar("ped");
        pedido.otId = otId;
        pedido.estado = "Solicitado";
        pedido.creadoPor = creadoPor;
        pedido.creadoEn = Instant.now().toString();
        pedido = pedidoRepository.save(pedido);

        for (ItemPedido item : items) {
            PedidoDetalle d = new PedidoDetalle();
            d.id = IdGenerator.generar("pd");
            d.pedidoId = pedido.id;
            d.productoId = item.productoId();
            d.cantidadSolicitada = item.cantidad();
            d.cantidadDespachada = 0;
            detalleRepository.save(d);
        }
        // Si la OT todavía no llegó a "Pedido de repuestos" (ej. se pidió antes de
        // terminar el diagnóstico), avanza sola — nunca retrocede lo ya avanzado.
        otService.avanzarSiCorresponde(otId, "Pedido de repuestos", creadoPor, "Pedido de repuestos generado");
        return pedido;
    }

    public record Resultado(boolean ok, String error) {}

    public Resultado aprobarParaAlmacen(String pedidoId, String usuarioId) {
        PedidoAlmacen pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        var cotizacion = cotizacionService.deOt(pedido.otId);
        if (cotizacion.isEmpty() || !cotizacion.get().autorizado) {
            return new Resultado(false, "El cliente aún no autoriza la cotización: no se puede enviar el pedido a Almacén.");
        }
        pedido.estado = "Aprobado";
        pedidoRepository.save(pedido);
        soporteService.registrarAuditoria(pedido.otId, usuarioId, "Pedido enviado a Almacén (presupuesto aceptado por el cliente)", null, null);

        OrdenTrabajo ot = otRepository.findById(pedido.otId).orElse(null);
        if (ot != null) {
            usuarioRepository.findAll().stream()
                    .filter(u -> "almacen".equals(u.rol) && u.activo)
                    .forEach(u -> soporteService.notificar(u.id, "Nuevo pedido por alistar: OT " + ot.numeroOT + ".", ot.id));
        }
        return new Resultado(true, null);
    }

    /** Un solo clic para Recepción: genera la cotización desde el pedido si no existe, la autoriza y aprueba el pedido. */
    public Resultado confirmarAceptacionYEnviar(String otId, String pedidoId, String usuarioId) {
        var cotizacionExistente = cotizacionService.deOt(otId);
        Cotizacion cotizacion;
        if (cotizacionExistente.isEmpty()) {
            List<PedidoDetalle> detalle = detalleRepository.findByPedidoId(pedidoId);
            if (detalle.isEmpty()) return new Resultado(false, "Este pedido no tiene productos.");
            List<ItemCotizacionDto> items = new ArrayList<>();
            for (PedidoDetalle d : detalle) {
                Producto p = productoRepository.findById(d.productoId).orElse(null);
                items.add(new ItemCotizacionDto(p != null ? p.nombre : "Producto", d.cantidadSolicitada, p != null ? p.precio : 0));
            }
            cotizacion = cotizacionService.generar(otId, items, usuarioId);
        } else {
            cotizacion = cotizacionExistente.get();
        }
        if (!cotizacion.autorizado) {
            cotizacionService.autorizar(cotizacion.id, usuarioId);
        }
        return aprobarParaAlmacen(pedidoId, usuarioId);
    }

    public Resultado despachar(String pedidoId, String usuarioId, String fotoDespacho) {
        PedidoAlmacen pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        if (!"Aprobado".equals(pedido.estado)) {
            return new Resultado(false, "Este pedido todavía no fue aprobado por Recepción tras la aceptación del cliente.");
        }
        List<PedidoDetalle> detalle = detalleRepository.findByPedidoId(pedidoId);
        for (PedidoDetalle d : detalle) {
            Producto p = productoRepository.findById(d.productoId).orElse(null);
            int faltante = d.cantidadSolicitada - d.cantidadDespachada;
            if (p != null && faltante > 0 && p.stockActual < faltante) {
                return new Resultado(false, "Stock insuficiente de \"" + p.nombre + "\" (disponible: " + p.stockActual + ", requerido: " + faltante + ").");
            }
        }
        for (PedidoDetalle d : detalle) {
            int faltante = d.cantidadSolicitada - d.cantidadDespachada;
            if (faltante <= 0) continue;
            Producto p = productoRepository.findById(d.productoId).orElseThrow();
            p.stockActual -= faltante;
            productoRepository.save(p);
            d.cantidadDespachada = d.cantidadSolicitada;
            detalleRepository.save(d);
            productoService.registrarMovimiento(p.id, "salida", faltante, pedido.otId, usuarioId);
        }
        pedido.estado = "Despachado";
        if (fotoDespacho != null) pedido.fotoDespacho = fotoDespacho;
        pedidoRepository.save(pedido);
        soporteService.registrarAuditoria(pedido.otId, usuarioId, "Despacho de pedido de almacén", null, null);

        OrdenTrabajo ot = otRepository.findById(pedido.otId).orElse(null);
        if (ot != null) {
            if (ot.trabajoIniciadoEn == null) {
                ot.trabajoIniciadoEn = Instant.now().toString();
                otRepository.save(ot);
                if (ot.mecanicoId != null) {
                    soporteService.notificar(ot.mecanicoId, "Almacén entregó los productos de tu OT " + ot.numeroOT + ". Se inició el temporizador de servicio.", ot.id);
                }
            }
            if (ot.asesorId != null) {
                soporteService.notificar(ot.asesorId, "El pedido de la OT " + ot.numeroOT + " ya está listo en Almacén.", ot.id);
            }
        }
        return new Resultado(true, null);
    }
}
