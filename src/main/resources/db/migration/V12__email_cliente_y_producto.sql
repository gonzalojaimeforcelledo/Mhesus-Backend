-- ============================================================================
-- MHESUS — Migración:
--   1) correo del cliente (se pide como obligatorio al crear/editar una OT)
--   2) productos: precio_anterior (se guarda solo al cambiar el precio) y
--      descuento_maximo (% máximo de descuento permitido para ese producto)
-- ============================================================================

ALTER TABLE clientes ADD COLUMN IF NOT EXISTS email VARCHAR(200);

ALTER TABLE productos ADD COLUMN IF NOT EXISTS precio_anterior NUMERIC(10,2);
ALTER TABLE productos ADD COLUMN IF NOT EXISTS descuento_maximo NUMERIC(5,2);
