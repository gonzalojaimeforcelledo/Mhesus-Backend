-- ============================================================================
-- MHESUS — Migración: fotos de las 4 caras de la moto al ingreso (antes solo
-- había una foto de ingreso). foto_ingreso ya existente pasa a representar la
-- cara frontal; se agregan las otras tres.
-- ============================================================================

ALTER TABLE ordenes_trabajo ADD COLUMN IF NOT EXISTS foto_ingreso_trasera TEXT;
ALTER TABLE ordenes_trabajo ADD COLUMN IF NOT EXISTS foto_ingreso_lateral_izq TEXT;
ALTER TABLE ordenes_trabajo ADD COLUMN IF NOT EXISTS foto_ingreso_lateral_der TEXT;
