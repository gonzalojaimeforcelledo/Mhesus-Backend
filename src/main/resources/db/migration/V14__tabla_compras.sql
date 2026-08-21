-- ============================================================================
-- MHESUS — Migración: módulo de Compras (com.mhesus.api.compras.domain).
-- Registro simple de compras a proveedores, necesario para calcular el IGV
-- a pagar del mes en Administración (débito fiscal de ventas menos crédito
-- fiscal de compras).
-- ============================================================================

CREATE TABLE compras (
    id                  VARCHAR(64) PRIMARY KEY,
    proveedor           VARCHAR(200) NOT NULL,
    descripcion         VARCHAR(500),
    numero_comprobante  VARCHAR(50),
    monto_total         NUMERIC(10,2) NOT NULL,
    igv                 NUMERIC(10,2) NOT NULL,
    fecha               VARCHAR(10) NOT NULL,  -- YYYY-MM-DD
    creado_por          VARCHAR(64) NOT NULL REFERENCES usuarios(id),
    creado_en           VARCHAR(40) NOT NULL
);
CREATE INDEX idx_compras_fecha ON compras(fecha);
