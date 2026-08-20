-- ============================================================================
-- MHESUS — Migración: agrega "Observación del asesor" a la OT — el registro
-- que hace el propio asesor de recepción al recibir la moto, aparte de lo
-- que el cliente reporta (observacion_cliente).
-- ============================================================================

ALTER TABLE ordenes_trabajo ADD COLUMN IF NOT EXISTS observacion_asesor VARCHAR(2000);
