package com.mhesus.api.ofertas.presentation;

import com.mhesus.api.ofertas.application.OfertaRequest;
import com.mhesus.api.ofertas.application.OfertaService;
import com.mhesus.api.ofertas.domain.Oferta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ofertas")
public class OfertaController {
    private final OfertaService ofertaService;

    public OfertaController(OfertaService ofertaService) {
        this.ofertaService = ofertaService;
    }

    private String usuarioId(HttpServletRequest req) {
        Object v = req.getAttribute("usuarioId");
        return v == null ? null : v.toString();
    }

    @GetMapping
    public List<Map<String, Object>> listar(@RequestParam(required = false) Boolean soloActivas) {
        List<Oferta> lista = Boolean.TRUE.equals(soloActivas) ? ofertaService.listarActivas() : ofertaService.listarTodas();
        return lista.stream().map(this::aMapa).toList();
    }

    @PostMapping
    public Map<String, Object> crear(@RequestBody OfertaRequest req, HttpServletRequest http) {
        return aMapa(ofertaService.crear(req, usuarioId(http)));
    }

    @PutMapping("/{id}")
    public Map<String, Object> actualizar(@PathVariable String id, @RequestBody OfertaRequest req) {
        return aMapa(ofertaService.actualizar(id, req));
    }

    @PatchMapping("/{id}/alternar-activa")
    public Map<String, Object> alternarActiva(@PathVariable String id) {
        return aMapa(ofertaService.alternarActiva(id));
    }

    private Map<String, Object> aMapa(Oferta o) {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", o.id);
        m.put("nombre", o.nombre);
        m.put("descripcion", o.descripcion);
        m.put("precioOferta", o.precioOferta);
        m.put("items", ofertaService.leerItems(o));
        m.put("activo", o.activo);
        m.put("creadoPor", o.creadoPor);
        m.put("creadoEn", o.creadoEn);
        return m;
    }
}
