-- ============================================================================
-- MHESUS — Migración: módulo de Facturación (com.mhesus.api.ventas.domain).
-- Boletas, facturas, notas de crédito/débito, proformas y guías de remisión
-- generadas internamente. No transmite a SUNAT (ver comentario en Venta.java
-- y el README) — es el sistema de ventas interno del taller.
-- ============================================================================

CREATE TABLE ventas (
    id                 VARCHAR(64) PRIMARY KEY,
    tipo               VARCHAR(20) NOT NULL,  -- FACTURA | BOLETA | NOTA_CREDITO | NOTA_DEBITO | PROFORMA | GUIA_REMISION
    serie              VARCHAR(10) NOT NULL,
    numero             INTEGER NOT NULL,
    ot_id              VARCHAR(64) REFERENCES ordenes_trabajo(id),
    cliente_id         VARCHAR(64) REFERENCES clientes(id),
    cliente_nombre     VARCHAR(200),
    cliente_documento  VARCHAR(20),
    detalle_json       TEXT NOT NULL DEFAULT '[]',
    subtotal           NUMERIC(10,2) NOT NULL DEFAULT 0,
    igv                NUMERIC(10,2) NOT NULL DEFAULT 0,
    total              NUMERIC(10,2) NOT NULL DEFAULT 0,
    estado             VARCHAR(20) NOT NULL DEFAULT 'EMITIDA',
    creado_por         VARCHAR(64) NOT NULL REFERENCES usuarios(id),
    creado_en          VARCHAR(40) NOT NULL
);
CREATE INDEX idx_ventas_tipo_serie ON ventas(tipo, serie);
CREATE INDEX idx_ventas_creado_en ON ventas(creado_en DESC);
CREATE INDEX idx_ventas_ot ON ventas(ot_id);
