package com.mhesus.api.tareas.application;

import com.mhesus.api.shared.util.IdGenerator;
import com.mhesus.api.soporte.application.SoporteService;
import com.mhesus.api.tareas.domain.Tarea;
import com.mhesus.api.tareas.domain.TareaRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TareaService {
    private final TareaRepository tareaRepository;
    private final SoporteService soporteService;

    public TareaService(TareaRepository tareaRepository, SoporteService soporteService) {
        this.tareaRepository = tareaRepository;
        this.soporteService = soporteService;
    }

    public List<Tarea> listarPorRango(String desde, String hasta) {
        return tareaRepository.findByFechaBetweenOrderByFechaAscHoraAsc(desde, hasta);
    }

    public Tarea crear(TareaRequest req, String creadoPorId) {
        Tarea t = new Tarea();
        t.id = IdGenerator.generar("tarea");
        t.titulo = req.titulo();
        t.descripcion = req.descripcion();
        t.fecha = req.fecha();
        t.hora = req.hora();
        t.tipo = (req.tipo() == null || req.tipo().isBlank()) ? "nota" : req.tipo();
        t.motoId = req.motoId();
        t.creadoPor = creadoPorId;
        t.asignadoA = req.asignadoA();
        t.completada = false;
        t.creadoEn = Instant.now().toString();
        Tarea guardada = tareaRepository.save(t);

        // Si el administrador la asignó a otra persona (no a sí mismo), le avisamos por notificación.
        if (t.asignadoA != null && !t.asignadoA.equals(creadoPorId)) {
            soporteService.notificar(t.asignadoA, "Nueva tarea asignada: " + t.titulo + " (" + t.fecha + ")", null);
        }
        return guardada;
    }

    public Tarea asignar(String id, String asignadoA) {
        Tarea t = tareaRepository.findById(id).orElseThrow();
        t.asignadoA = asignadoA;
        Tarea guardada = tareaRepository.save(t);
        if (asignadoA != null) {
            soporteService.notificar(asignadoA, "Se te asignó la tarea: " + t.titulo + " (" + t.fecha + ")", null);
        }
        return guardada;
    }

    public Tarea completar(String id) {
        Tarea t = tareaRepository.findById(id).orElseThrow();
        t.completada = !t.completada;
        return tareaRepository.save(t);
    }

    public void eliminar(String id) {
        tareaRepository.deleteById(id);
    }
}
