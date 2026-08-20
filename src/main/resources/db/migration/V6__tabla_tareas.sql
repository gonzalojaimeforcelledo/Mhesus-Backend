-- ============================================================================
-- MHESUS — Migración: módulo de Calendario (com.mhesus.api.tareas.domain).
-- Notas, recordatorios y tareas asignadas para Recepción, Almacén y
-- Administración. tipo "recordatorio_moto" + moto_id se usa para avisar
-- cuándo le toca a una moto su siguiente atención.
-- ============================================================================

CREATE TABLE tareas (
    id           VARCHAR(64) PRIMARY KEY,
    titulo       VARCHAR(200) NOT NULL,
    descripcion  VARCHAR(2000),
    fecha        VARCHAR(10) NOT NULL,   -- YYYY-MM-DD
    hora         VARCHAR(5),             -- HH:mm
    tipo         VARCHAR(30) NOT NULL DEFAULT 'nota',
    moto_id      VARCHAR(64) REFERENCES motocicletas(id),
    creado_por   VARCHAR(64) NOT NULL REFERENCES usuarios(id),
    asignado_a   VARCHAR(64) REFERENCES usuarios(id),
    completada   BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en    VARCHAR(40) NOT NULL
);
CREATE INDEX idx_tareas_fecha ON tareas(fecha);
CREATE INDEX idx_tareas_asignado ON tareas(asignado_a);
