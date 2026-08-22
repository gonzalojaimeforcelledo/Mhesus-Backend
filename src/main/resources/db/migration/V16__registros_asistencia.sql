-- ============================================================================
-- MHESUS — Migración: módulo de Asistencia. Llegada y salida validadas por
-- IP del WiFi del taller (ver AsistenciaService); almuerzo sin esa
-- restricción.
-- ============================================================================

CREATE TABLE registros_asistencia (
    id                    VARCHAR(64) PRIMARY KEY,
    usuario_id            VARCHAR(64) NOT NULL REFERENCES usuarios(id),
    fecha                 VARCHAR(10) NOT NULL,
    hora_llegada          VARCHAR(8),
    hora_inicio_almuerzo  VARCHAR(8),
    hora_fin_almuerzo     VARCHAR(8),
    hora_salida           VARCHAR(8),
    creado_en             VARCHAR(40) NOT NULL,
    UNIQUE (usuario_id, fecha)
);
CREATE INDEX idx_asistencia_usuario_fecha ON registros_asistencia(usuario_id, fecha);
