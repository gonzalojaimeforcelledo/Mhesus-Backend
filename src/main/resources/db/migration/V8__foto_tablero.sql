-- ============================================================================
-- MHESUS — Migración: 5ta foto de ingreso (tablero encendido) + marca de si
-- el tablero no encendió (en ese caso la foto no es obligatoria, pero el
-- asesor debe anotarlo en observacion_asesor).
-- ============================================================================

ALTER TABLE ordenes_trabajo ADD COLUMN IF NOT EXISTS foto_tablero TEXT;
ALTER TABLE ordenes_trabajo ADD COLUMN IF NOT EXISTS tablero_no_enciende BOOLEAN NOT NULL DEFAULT FALSE;
