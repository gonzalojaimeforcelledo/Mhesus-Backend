package com.mhesus.api.tareas.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TareaRepository extends JpaRepository<Tarea, String> {
    List<Tarea> findByFechaBetweenOrderByFechaAscHoraAsc(String desde, String hasta);
    List<Tarea> findByAsignadoAOrderByFechaAscHoraAsc(String asignadoA);
}
