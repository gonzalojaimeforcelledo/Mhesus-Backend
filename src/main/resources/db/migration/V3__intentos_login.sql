-- ============================================================================
-- MHESUS — Migración: control de intentos de login (bloqueo de 5 minutos
-- al tercer intento fallido). Vive en el módulo "auth".
-- Persistido en base de datos a propósito: así el bloqueo sigue vigente
-- aunque el backend se reinicie (a diferencia de guardarlo solo en memoria).
-- ============================================================================

CREATE TABLE intentos_login (
    usuario         VARCHAR(50) PRIMARY KEY,
    intentos        INTEGER NOT NULL DEFAULT 0,
    bloqueado_hasta VARCHAR(40)
);
