-- ============================================================================
-- MHESUS — Migración: compatibilidad de producto con moto (marca, modelo,
-- submodelo y rango de años) — para poder filtrar el catálogo por qué
-- motos maneja el taller y para qué repuestos sirven.
-- ============================================================================

ALTER TABLE productos ADD COLUMN IF NOT EXISTS marca_moto VARCHAR(50);
ALTER TABLE productos ADD COLUMN IF NOT EXISTS modelo_moto VARCHAR(50);
ALTER TABLE productos ADD COLUMN IF NOT EXISTS submodelo_moto VARCHAR(50);
ALTER TABLE productos ADD COLUMN IF NOT EXISTS anio_desde INTEGER;
ALTER TABLE productos ADD COLUMN IF NOT EXISTS anio_hasta INTEGER;
CREATE INDEX IF NOT EXISTS idx_productos_marca_moto ON productos(marca_moto);

-- ============================================================================
-- Módulo de Deudas (Administración) — deudas por cobrar (clientes) y deudas
-- de banco (préstamos/financiamiento del taller).
-- ============================================================================
CREATE TABLE deudas (
    id                VARCHAR(64) PRIMARY KEY,
    tipo              VARCHAR(20) NOT NULL,   -- POR_COBRAR | BANCO
    nombre            VARCHAR(200) NOT NULL,
    descripcion       VARCHAR(500),
    cliente_id        VARCHAR(64) REFERENCES clientes(id),
    monto_original    NUMERIC(10,2) NOT NULL,
    monto_pendiente   NUMERIC(10,2) NOT NULL,
    fecha_vencimiento VARCHAR(10),
    estado            VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    creado_por        VARCHAR(64) NOT NULL REFERENCES usuarios(id),
    creado_en         VARCHAR(40) NOT NULL
);
CREATE INDEX idx_deudas_tipo ON deudas(tipo, estado);

