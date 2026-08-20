package com.mhesus.api.tareas.application;

public record TareaRequest(
    String titulo, String descripcion, String fecha, String hora,
    String tipo, String motoId, String asignadoA
) {}
