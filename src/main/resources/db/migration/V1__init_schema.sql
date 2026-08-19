-- ============================================================================
-- MHESUS — Migración inicial: esquema de base de datos (PostgreSQL)
--
-- Las tablas están agrupadas por MÓDULO / BOUNDED CONTEXT, en el mismo orden
-- que la estructura DDD del backend (com.mhesus.api.<modulo>.domain):
-- auth, clientes, ot, almacen, cotizacion, soporte (shared kernel).
-- Corresponde 1 a 1 a las entidades JPA. Flyway corre esto automáticamente
-- al arrancar la app (spring.flyway.enabled: true) — no hace falta correrlo
-- a mano en ningún panel de SQL.
-- ============================================================================

-- Empieza borrando las tablas por si la migración se reintenta sobre un
-- esquema parcial (orden inverso a las dependencias, para no chocar con
-- las llaves foráneas).
DROP TABLE IF EXISTS notificaciones CASCADE;
DROP TABLE IF EXISTS auditoria CASCADE;
DROP TABLE IF EXISTS cotizaciones CASCADE;
DROP TABLE IF EXISTS movimientos_inventario CASCADE;
DROP TABLE IF EXISTS pedido_detalle CASCADE;
DROP TABLE IF EXISTS pedidos_almacen CASCADE;
DROP TABLE IF EXISTS productos CASCADE;
DROP TABLE IF EXISTS diagnosticos CASCADE;
DROP TABLE IF EXISTS ordenes_trabajo CASCADE;
DROP TABLE IF EXISTS motocicletas CASCADE;
DROP TABLE IF EXISTS clientes CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;

-- ============================================================================
-- MÓDULO: auth  (com.mhesus.api.auth.domain)
-- Autenticación, sesión por rol, permisos RBAC. RNF-06, RNF-07.
-- ============================================================================
CREATE TABLE usuarios (
    id            VARCHAR(64) PRIMARY KEY,
    nombre        VARCHAR(150) NOT NULL,
    usuario       VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    rol           VARCHAR(30)  NOT NULL,  -- recepcion | mecanico | almacen | jefe_taller | administracion
    activo        BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ============================================================================
-- MÓDULO: clientes  (com.mhesus.api.clientes.domain)
-- Alta/búsqueda de clientes por DNI, motocicletas asociadas. RF-01, RF-02, RF-03.
-- ============================================================================
CREATE TABLE clientes (
    id         VARCHAR(64) PRIMARY KEY,
    dni        VARCHAR(15)  NOT NULL UNIQUE,
    nombres    VARCHAR(150) NOT NULL,
    apellidos  VARCHAR(150) NOT NULL,
    celular    VARCHAR(20)  NOT NULL,
    direccion  VARCHAR(255),
    creado_en  VARCHAR(40)  NOT NULL
);

CREATE TABLE motocicletas (
    id          VARCHAR(64) PRIMARY KEY,
    cliente_id  VARCHAR(64) NOT NULL REFERENCES clientes(id),
    placa       VARCHAR(15) NOT NULL UNIQUE,
    marca       VARCHAR(60) NOT NULL,
    modelo      VARCHAR(60) NOT NULL,
    anio        INTEGER     NOT NULL,
    km_actual   INTEGER     NOT NULL DEFAULT 0
);
CREATE INDEX idx_motocicletas_cliente ON motocicletas(cliente_id);
CREATE INDEX idx_motocicletas_placa ON motocicletas(placa);

-- ============================================================================
-- MÓDULO: ot  (com.mhesus.api.ot.domain)
-- Ciclo de vida completo de la Orden de Trabajo. RF-04 a RF-14, RF-20.
-- ============================================================================
CREATE TABLE ordenes_trabajo (
    id                     VARCHAR(64) PRIMARY KEY,
    numero_ot              VARCHAR(20)  NOT NULL UNIQUE,
    cliente_id             VARCHAR(64)  NOT NULL REFERENCES clientes(id),
    moto_id                VARCHAR(64)  NOT NULL REFERENCES motocicletas(id),
    mecanico_id            VARCHAR(64)  REFERENCES usuarios(id),
    asesor_id              VARCHAR(64)  REFERENCES usuarios(id),
    estado                 VARCHAR(40)  NOT NULL DEFAULT 'Creada',
    nivel_combustible      VARCHAR(5)   NOT NULL DEFAULT '1/2',  -- E | 1/4 | 1/2 | 3/4 | F
    observacion_cliente    TEXT,
    servicio_a_realizar    TEXT,
    creado_en              VARCHAR(40)  NOT NULL,
    trabajo_iniciado_en    VARCHAR(40),
    trabajo_finalizado_en  VARCHAR(40),
    foto_ingreso           TEXT  -- imagen en base64 (evidencia de recepción)
);
CREATE INDEX idx_ot_cliente ON ordenes_trabajo(cliente_id);
CREATE INDEX idx_ot_moto ON ordenes_trabajo(moto_id);
CREATE INDEX idx_ot_mecanico ON ordenes_trabajo(mecanico_id);
CREATE INDEX idx_ot_estado ON ordenes_trabajo(estado);

CREATE TABLE diagnosticos (
    id                 VARCHAR(64) PRIMARY KEY,
    ot_id              VARCHAR(64) NOT NULL UNIQUE REFERENCES ordenes_trabajo(id),
    diagnostico        TEXT,
    sugerencias        TEXT,
    mecanico_nombre    VARCHAR(150),
    creado_en          VARCHAR(40) NOT NULL,
    foto_diagnostico   TEXT  -- imagen en base64 (evidencia del mecánico)
);

-- ============================================================================
-- MÓDULO: almacen  (com.mhesus.api.almacen.domain)
-- Catálogo de productos, pedidos, despacho, movimientos de stock.
-- RF-07, RF-08, RF-09, RF-18, RF-19.
-- ============================================================================
CREATE TABLE productos (
    id             VARCHAR(64) PRIMARY KEY,
    codigo         VARCHAR(50)  NOT NULL UNIQUE,
    codigo_barras  VARCHAR(50),
    nombre         VARCHAR(200) NOT NULL,
    categoria      VARCHAR(100),
    precio         NUMERIC(10,2) NOT NULL DEFAULT 0,
    stock_actual   INTEGER      NOT NULL DEFAULT 0,
    stock_minimo   INTEGER      NOT NULL DEFAULT 0,
    lugar          VARCHAR(100)  -- ubicación física en el almacén
);

CREATE TABLE pedidos_almacen (
    id             VARCHAR(64) PRIMARY KEY,
    ot_id          VARCHAR(64) NOT NULL REFERENCES ordenes_trabajo(id),
    estado         VARCHAR(20) NOT NULL DEFAULT 'Solicitado',  -- Solicitado | Aprobado | Despachado | Cancelado
    creado_por     VARCHAR(64) REFERENCES usuarios(id),
    creado_en      VARCHAR(40) NOT NULL,
    foto_despacho  TEXT  -- imagen en base64 (evidencia de almacén)
);
CREATE INDEX idx_pedidos_ot ON pedidos_almacen(ot_id);

CREATE TABLE pedido_detalle (
    id                    VARCHAR(64) PRIMARY KEY,
    pedido_id             VARCHAR(64) NOT NULL REFERENCES pedidos_almacen(id),
    producto_id           VARCHAR(64) NOT NULL REFERENCES productos(id),
    cantidad_solicitada   INTEGER NOT NULL,
    cantidad_despachada   INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_pedido_detalle_pedido ON pedido_detalle(pedido_id);

CREATE TABLE movimientos_inventario (
    id            VARCHAR(64) PRIMARY KEY,
    producto_id   VARCHAR(64) NOT NULL REFERENCES productos(id),
    tipo          VARCHAR(20) NOT NULL,  -- ingreso | salida | ajuste
    cantidad      INTEGER NOT NULL,
    ot_id         VARCHAR(64) REFERENCES ordenes_trabajo(id),
    usuario_id    VARCHAR(64) REFERENCES usuarios(id),
    creado_en     VARCHAR(40) NOT NULL
);
CREATE INDEX idx_movimientos_producto ON movimientos_inventario(producto_id);

-- ============================================================================
-- MÓDULO: cotizacion  (com.mhesus.api.cotizacion.domain)
-- Cálculo de cotización y autorización del cliente. RF-12, RF-13.
-- El detalle de ítems {descripcion, cantidad, precioUnitario} se guarda como JSON.
-- ============================================================================
CREATE TABLE cotizaciones (
    id             VARCHAR(64) PRIMARY KEY,
    ot_id          VARCHAR(64) NOT NULL UNIQUE REFERENCES ordenes_trabajo(id),
    detalle_json   TEXT NOT NULL DEFAULT '[]',
    monto_total    NUMERIC(10,2) NOT NULL DEFAULT 0,
    autorizado     BOOLEAN NOT NULL DEFAULT FALSE,
    autorizado_en  VARCHAR(40)
);

-- ============================================================================
-- MÓDULO: soporte  (com.mhesus.api.soporte.domain) — shared kernel
-- Auditoría y notificaciones: cruzan todos los demás módulos.
-- ============================================================================
CREATE TABLE auditoria (
    id               VARCHAR(64) PRIMARY KEY,
    ot_id            VARCHAR(64) REFERENCES ordenes_trabajo(id),
    usuario_id       VARCHAR(64) REFERENCES usuarios(id),
    accion           VARCHAR(500) NOT NULL,
    estado_anterior  VARCHAR(40),
    estado_nuevo     VARCHAR(40),
    creado_en        VARCHAR(40) NOT NULL
);
CREATE INDEX idx_auditoria_ot ON auditoria(ot_id);
CREATE INDEX idx_auditoria_creado ON auditoria(creado_en DESC);

CREATE TABLE notificaciones (
    id           VARCHAR(64) PRIMARY KEY,
    usuario_id   VARCHAR(64) NOT NULL REFERENCES usuarios(id),
    mensaje      VARCHAR(500) NOT NULL,
    ot_id        VARCHAR(64) REFERENCES ordenes_trabajo(id),
    leida        BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en    VARCHAR(40) NOT NULL
);
CREATE INDEX idx_notificaciones_usuario ON notificaciones(usuario_id, creado_en DESC);
