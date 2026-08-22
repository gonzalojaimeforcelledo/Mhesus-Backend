-- ============================================================================
-- MHESUS — Migración: datos de contacto y trazabilidad de fechas para
-- deudas por cobrar (celular, dirección, garantía dejada, fecha en que se
-- otorgó la deuda) + control del recordatorio mensual automático.
-- ============================================================================

ALTER TABLE deudas ADD COLUMN celular VARCHAR(20);
ALTER TABLE deudas ADD COLUMN direccion VARCHAR(300);
ALTER TABLE deudas ADD COLUMN garantia VARCHAR(300);

-- Fecha en que se otorgó/solicitó la deuda (distinta de creado_en, que es el
-- timestamp técnico de cuándo se guardó el registro en el sistema — permite
-- registrar deudas antiguas con su fecha real de origen).
ALTER TABLE deudas ADD COLUMN fecha_inicio VARCHAR(10);
UPDATE deudas SET fecha_inicio = LEFT(creado_en, 10) WHERE fecha_inicio IS NULL;

-- Último mes (YYYY-MM) en que se envió el recordatorio automático de esta
-- deuda, para no notificar más de una vez por mes.
ALTER TABLE deudas ADD COLUMN ultima_notificacion_mes VARCHAR(7);
