package com.mhesus.api.compras.application;

import com.mhesus.api.compras.domain.Compra;
import com.mhesus.api.compras.domain.CompraRepository;
import com.mhesus.api.shared.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CompraService {
    private static final double TASA_IGV = 0.18;

    private final CompraRepository compraRepository;

    public CompraService(CompraRepository compraRepository) {
        this.compraRepository = compraRepository;
    }

    public List<Compra> listar() {
        return compraRepository.findAll();
    }

    public Compra crear(CompraRequest req, String usuarioId) {
        Compra c = new Compra();
        c.id = IdGenerator.generar("compra");
        c.proveedor = req.proveedor();
        c.descripcion = req.descripcion();
        c.numeroComprobante = req.numeroComprobante();
        c.montoTotal = req.montoTotal();
        c.igv = round2(req.montoTotal() - req.montoTotal() / (1 + TASA_IGV));
        c.fecha = req.fecha();
        c.creadoPor = usuarioId;
        c.creadoEn = Instant.now().toString();
        return compraRepository.save(c);
    }

    public void eliminar(String id) {
        compraRepository.deleteById(id);
    }

    /** IGV de compras (crédito fiscal) del rango de fechas dado. */
    public double igvDelPeriodo(String desde, String hasta) {
        return round2(compraRepository.findByFechaBetweenOrderByFechaDesc(desde, hasta).stream().mapToDouble(c -> c.igv).sum());
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
