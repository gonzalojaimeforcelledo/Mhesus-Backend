package com.mhesus.api.tareas.presentation;

import com.mhesus.api.tareas.application.AsignarTareaRequest;
import com.mhesus.api.tareas.application.TareaRequest;
import com.mhesus.api.tareas.application.TareaService;
import com.mhesus.api.tareas.domain.Tarea;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tareas")
public class TareaController {
    private final TareaService tareaService;

    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    @GetMapping
    public List<Tarea> listar(@RequestParam String desde, @RequestParam String hasta) {
        return tareaService.listarPorRango(desde, hasta);
    }

    @PostMapping
    public Tarea crear(@RequestBody TareaRequest req, Principal principal) {
        return tareaService.crear(req, principal.getName());
    }

    @PatchMapping("/{id}/asignar")
    public Tarea asignar(@PathVariable String id, @RequestBody AsignarTareaRequest req) {
        return tareaService.asignar(id, req.asignadoA());
    }

    @PatchMapping("/{id}/completar")
    public Tarea completar(@PathVariable String id) {
        return tareaService.completar(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        tareaService.eliminar(id);
    }
}
