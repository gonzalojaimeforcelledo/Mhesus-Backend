package com.mhesus.api.ventas.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhesus.api.almacen.domain.Producto;
import com.mhesus.api.almacen.domain.ProductoRepository;
import com.mhesus.api.shared.util.IdGenerator;
import com.mhesus.api.ventas.domain.Venta;
import com.mhesus.api.ventas.domain.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

/**
 * MHESUS calcula todo (correlativos, IGV, totales) y guarda el comprobante,
 * pero NO lo transmite a SUNAT — esto es un sistema de ventas interno. Para
 * que un comprobante tenga validez legal ante SUNAT hace falta un operador
 * autorizado (OSE/PSE, ej. Nubefact/Efact) o el sistema COMPAC que ya usan;
 * ese paso queda como punto de integración pendiente, documentado en el
 * README, para conectar con credenciales reales cuando estén disponibles.
 */
@Service
public class VentaService {
    private static final double TASA_IGV = 0.18;

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VentaService(VentaRepository ventaRepository, ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
    }

    public List<Venta> listar() {
        return ventaRepository.findAllByOrderByCreadoEnDesc();
    }

    public Optional<Venta> porOt(String otId) {
        return ventaRepository.findByOtId(otId);
    }

    private String siguienteNumero(String tipo, String serie) {
        long cantidad = ventaRepository.countByTipoAndSerie(tipo, serie);
        return String.valueOf(cantidad + 1);
    }

    @Transactional
    public Venta crear(VentaRequest req, String usuarioId) {
        // El precio de cada ítem ya incluye IGV (precio final de venta al público,
        // como se maneja en el catálogo de Almacén) — se extrae el IGV desde el total,
        // no se suma aparte, para que el total coincida con lo que el cliente ve pagar.
        double total = req.items().stream().mapToDouble(i -> i.cantidad() * i.precioUnitario()).sum();
        double subtotal = round2(total / (1 + TASA_IGV));
        double igv = round2(total - subtotal);

        // Descuenta stock por cada ítem vinculado a un producto del catálogo (los
        // ítems de texto libre, ej. "Mano de obra", no tienen productoId y no
        // afectan el inventario). Se valida TODO primero, para no dejar la venta
        // a medias si algún producto no alcanza.
        for (ItemVentaDto item : req.items()) {
            if (item.productoId() == null || item.productoId().isBlank()) continue;
            Producto p = productoRepository.findById(item.productoId())
                    .orElseThrow(() -> new StockInsuficienteException("Producto no encontrado en el catálogo."));
            if (p.stockActual < item.cantidad()) {
                throw new StockInsuficienteException(
                        "Stock insuficiente de \"" + p.nombre + "\" (disponible: " + p.stockActual + ", pedido: " + item.cantidad() + ").");
            }
        }
        for (ItemVentaDto item : req.items()) {
            if (item.productoId() == null || item.productoId().isBlank()) continue;
            Producto p = productoRepository.findById(item.productoId()).orElseThrow();
            p.stockActual -= (int) Math.round(item.cantidad());
            productoRepository.save(p);
        }

        Venta v = new Venta();
        v.id = IdGenerator.generar("vta");
        v.tipo = req.tipo();
        v.serie = req.serie() == null || req.serie().isBlank() ? seriePorDefecto(req.tipo()) : req.serie();
        v.numero = Integer.parseInt(siguienteNumero(req.tipo(), v.serie));
        v.otId = req.otId();
        v.clienteId = req.clienteId();
        v.clienteNombre = req.clienteNombre();
        v.clienteDocumento = req.clienteDocumento();
        try {
            v.detalleJson = objectMapper.writeValueAsString(req.items());
        } catch (Exception e) {
            v.detalleJson = "[]";
        }
        v.subtotal = subtotal;
        v.igv = igv;
        v.total = round2(total);
        v.estado = "EMITIDA";
        v.creadoPor = usuarioId;
        v.creadoEn = Instant.now().toString();
        return ventaRepository.save(v);
    }

    private String seriePorDefecto(String tipo) {
        return switch (tipo) {
            case "FACTURA" -> "FF01";
            case "BOLETA" -> "BB01";
            case "NOTA_CREDITO" -> "NC01";
            case "NOTA_DEBITO" -> "ND01";
            case "GUIA_REMISION" -> "TT01";
            default -> "PRO01"; // PROFORMA
        };
    }

    public Venta anular(String id) {
        Venta v = ventaRepository.findById(id).orElseThrow();
        if ("ANULADA".equals(v.estado)) return v; // ya estaba anulada, no repite la devolución de stock
        v.estado = "ANULADA";
        // Devuelve al stock lo que esta venta había descontado.
        for (ItemVentaDto item : leerDetalle(v)) {
            if (item.productoId() == null || item.productoId().isBlank()) continue;
            productoRepository.findById(item.productoId()).ifPresent(p -> {
                p.stockActual += (int) Math.round(item.cantidad());
                productoRepository.save(p);
            });
        }
        return ventaRepository.save(v);
    }

    /** Se lanza cuando un ítem de la venta no tiene stock suficiente en el catálogo de Almacén. */
    public static class StockInsuficienteException extends RuntimeException {
        public StockInsuficienteException(String mensaje) { super(mensaje); }
    }

    public List<ItemVentaDto> leerDetalle(Venta v) {
        try {
            return List.of(objectMapper.readValue(v.detalleJson, ItemVentaDto[].class));
        } catch (Exception e) {
            return List.of();
        }
    }

    public ResumenDiaResponse resumenDelDia() {
        LocalDate hoy = LocalDate.now(ZoneOffset.UTC);
        String desde = hoy.atStartOfDay(ZoneOffset.UTC).toInstant().toString();
        String hasta = hoy.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toString();
        List<Venta> delDia = ventaRepository.findByCreadoEnBetweenOrderByCreadoEnDesc(desde, hasta).stream()
                .filter(v -> !"ANULADA".equals(v.estado))
                .filter(v -> !"PROFORMA".equals(v.tipo))
                .toList();

        double total = delDia.stream().mapToDouble(v -> v.total).sum();
        double igvTotal = delDia.stream().mapToDouble(v -> v.igv).sum();
        int emisiones = delDia.size();
        double promedio = emisiones == 0 ? 0 : round2(total / emisiones);

        Map<String, ResumenDiaResponse.ConteoTipo> porTipo = new LinkedHashMap<>();
        for (String tipo : List.of("FACTURA", "BOLETA", "NOTA_DEBITO", "NOTA_CREDITO")) {
            List<Venta> deEseTipo = delDia.stream().filter(v -> tipo.equals(v.tipo)).toList();
            double montoTipo = deEseTipo.stream().mapToDouble(v -> v.total).sum();
            porTipo.put(tipo, new ResumenDiaResponse.ConteoTipo(round2(montoTipo), deEseTipo.size()));
        }

        String ultimaEmision = delDia.isEmpty() ? null : delDia.get(0).creadoEn;
        return new ResumenDiaResponse(round2(total), emisiones, promedio, round2(igvTotal), porTipo, ultimaEmision);
    }

    /** Ventas emitidas por mes en el año dado (para el reporte de "Ventas por mes"). */
    public Map<String, Double> ventasPorMes(int anio) {
        Map<String, Double> resultado = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) resultado.put("%d-%02d".formatted(anio, m), 0.0);
        for (Venta v : ventaRepository.findAllByOrderByCreadoEnDesc()) {
            if ("ANULADA".equals(v.estado) || "PROFORMA".equals(v.tipo)) continue;
            String clave = v.creadoEn.substring(0, 7); // "YYYY-MM"
            if (clave.startsWith(String.valueOf(anio))) {
                resultado.merge(clave, v.total, Double::sum);
            }
        }
        resultado.replaceAll((k, val) -> round2(val));
        return resultado;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
