package com.mhesus.api.deudas.application;

import com.mhesus.api.deudas.domain.Deuda;
import com.mhesus.api.deudas.domain.DeudaRepository;
import com.mhesus.api.shared.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class DeudaService {
    private final DeudaRepository deudaRepository;

    public DeudaService(DeudaRepository deudaRepository) {
        this.deudaRepository = deudaRepository;
    }

    public List<Deuda> listar() {
        return deudaRepository.findAll();
    }

    public List<Deuda> porTipo(String tipo) {
        return deudaRepository.findByTipoOrderByCreadoEnDesc(tipo);
    }

    public Deuda crear(DeudaRequest req, String usuarioId) {
        Deuda d = new Deuda();
        d.id = IdGenerator.generar("deuda");
        d.tipo = req.tipo();
        d.nombre = req.nombre();
        d.descripcion = req.descripcion();
        d.clienteId = req.clienteId();
        d.montoOriginal = req.montoOriginal();
        d.montoPendiente = req.montoOriginal();
        d.fechaVencimiento = req.fechaVencimiento();
        d.estado = "PENDIENTE";
        d.creadoPor = usuarioId;
        d.creadoEn = Instant.now().toString();
        return deudaRepository.save(d);
    }

    /** Registra un abono/pago parcial (o total) — si el pendiente llega a 0, la marca como pagada. */
    public Deuda abonar(String id, double monto) {
        Deuda d = deudaRepository.findById(id).orElseThrow();
        d.montoPendiente = Math.max(0, round2(d.montoPendiente - monto));
        if (d.montoPendiente == 0) d.estado = "PAGADA";
        return deudaRepository.save(d);
    }

    public void eliminar(String id) {
        deudaRepository.deleteById(id);
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
