-- ============================================================================
-- MHESUS — Migración: nota/motivo libre en movimientos de inventario, para
-- el ingreso de stock por Excel (ej. "Pedido ingresado", "Corrección de
-- inventario").
-- ============================================================================

ALTER TABLE movimientos_inventario ADD COLUMN IF NOT EXISTS nota VARCHAR(300);
