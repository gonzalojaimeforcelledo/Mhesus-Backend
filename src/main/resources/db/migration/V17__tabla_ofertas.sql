-- ============================================================================
-- MHESUS — Migración: módulo de Ofertas. Combos de productos del catálogo
-- vendidos como un solo ítem a precio especial; no tienen stock propio — al
-- venderse, el stock se descuenta de cada producto que las compone (ver
-- VentaService.crear/anular).
-- ============================================================================

CREATE TABLE ofertas (
    id             VARCHAR(64) PRIMARY KEY,
    nombre         VARCHAR(200) NOT NULL,
    descripcion    VARCHAR(500),
    precio_oferta  NUMERIC(10,2) NOT NULL,
    items_json     TEXT NOT NULL DEFAULT '[]',
    activo         BOOLEAN NOT NULL DEFAULT TRUE,
    creado_por     VARCHAR(64) NOT NULL REFERENCES usuarios(id),
    creado_en      VARCHAR(40) NOT NULL
);
