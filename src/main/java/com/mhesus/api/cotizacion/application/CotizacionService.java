package com.mhesus.api.cotizacion.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhesus.api.cotizacion.application.ItemCotizacionDto;
import com.mhesus.api.cotizacion.domain.Cotizacion;
import com.mhesus.api.cotizacion.domain.CotizacionRepository;
import com.mhesus.api.ot.application.OtService;
import com.mhesus.api.shared.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class CotizacionService {
    private final CotizacionRepository cotizacionRepository;
    private final OtService otService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CotizacionService(CotizacionRepository cotizacionRepository, OtService otService) {
        this.cotizacionRepository = cotizacionRepository;
        this.otService = otService;
    }

    public Optional<Cotizacion> deOt(String otId) {
        return cotizacionRepository.findByOtId(otId);
    }

    public List<Cotizacion> listar() {
        return cotizacionRepository.findAll();
    }

    public List<ItemCotizacionDto> leerDetalle(Cotizacion c) {
        try {
            return List.of(objectMapper.readValue(c.detalleJson, ItemCotizacionDto[].class));
        } catch (Exception e) {
            return List.of();
        }
    }

    public Cotizacion generar(String otId, List<ItemCotizacionDto> detalle, String usuarioId) {
        double total = detalle.stream().mapToDouble(i -> i.cantidad() * i.precioUnitario()).sum();
        Cotizacion c = cotizacionRepository.findByOtId(otId).orElseGet(Cotizacion::new);
        c.id = c.id == null ? IdGenerator.generar("cot") : c.id;
        c.otId = otId;
        try {
            c.detalleJson = objectMapper.writeValueAsString(detalle);
        } catch (Exception e) {
            c.detalleJson = "[]";
        }
        c.montoTotal = total;
        if (c.autorizado != true) {
            c.autorizado = false;
            c.autorizadoEn = null;
        }
        Cotizacion guardada = cotizacionRepository.save(c);
        otService.avanzarSiCorresponde(otId, "En espera de autorización", usuarioId, "Cotización generada, en espera de autorización del cliente");
        return guardada;
    }

    public Cotizacion autorizar(String id, String usuarioId) {
        Cotizacion c = cotizacionRepository.findById(id).orElseThrow();
        c.autorizado = true;
        c.autorizadoEn = Instant.now().toString();
        Cotizacion guardada = cotizacionRepository.save(c);
        otService.avanzarSiCorresponde(c.otId, "En ejecución", usuarioId, "Cliente autorizó la cotización");
        return guardada;
    }
}
