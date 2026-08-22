package com.mhesus.api.ofertas.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhesus.api.ofertas.domain.Oferta;
import com.mhesus.api.ofertas.domain.OfertaRepository;
import com.mhesus.api.shared.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class OfertaService {
    private final OfertaRepository ofertaRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OfertaService(OfertaRepository ofertaRepository) {
        this.ofertaRepository = ofertaRepository;
    }

    public List<Oferta> listarActivas() {
        return ofertaRepository.findAll().stream().filter(o -> o.activo).toList();
    }

    public List<Oferta> listarTodas() {
        return ofertaRepository.findAll();
    }

    public Oferta crear(OfertaRequest req, String usuarioId) {
        Oferta o = new Oferta();
        o.id = IdGenerator.generar("oferta");
        aplicar(o, req);
        o.creadoPor = usuarioId;
        o.creadoEn = Instant.now().toString();
        return ofertaRepository.save(o);
    }

    public Oferta actualizar(String id, OfertaRequest req) {
        Oferta o = ofertaRepository.findById(id).orElseThrow();
        aplicar(o, req);
        return ofertaRepository.save(o);
    }

    private void aplicar(Oferta o, OfertaRequest req) {
        o.nombre = req.nombre();
        o.descripcion = req.descripcion();
        o.precioOferta = req.precioOferta();
        try {
            o.itemsJson = objectMapper.writeValueAsString(req.items());
        } catch (Exception e) {
            o.itemsJson = "[]";
        }
    }

    public Oferta alternarActiva(String id) {
        Oferta o = ofertaRepository.findById(id).orElseThrow();
        o.activo = !o.activo;
        return ofertaRepository.save(o);
    }

    public List<ItemOfertaDto> leerItems(Oferta o) {
        try {
            return List.of(objectMapper.readValue(o.itemsJson, ItemOfertaDto[].class));
        } catch (Exception e) {
            return List.of();
        }
    }
}
