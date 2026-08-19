-- ============================================================================
-- MHESUS — Migración: datos de demostración
-- Mismos usuarios/clientes/productos que siembra SeedDataRunner al arrancar
-- la app — pero como esto corre ANTES (Flyway corre antes que Hibernate),
-- SeedDataRunner detecta que la tabla de usuarios ya no está vacía y no
-- duplica nada.
-- Contraseña para TODOS los usuarios: demo1234
-- (hash BCrypt real, generado y verificado con la palabra "demo1234")
-- ============================================================================

INSERT INTO usuarios (id, nombre, usuario, password_hash, rol, activo) VALUES
    ('u_recepcion', 'Carla Ramos',            'recepcion', '$2b$10$uGIkFpawK.rw/gbZbkuFEu5e6lMfxF/bh4ZbjekvQByu6TaV36uuu', 'recepcion',      TRUE),
    ('u_mecanico1', 'Jhon Quispe',             'mecanico',  '$2b$10$uGIkFpawK.rw/gbZbkuFEu5e6lMfxF/bh4ZbjekvQByu6TaV36uuu', 'mecanico',       TRUE),
    ('u_mecanico2', 'Luis Falcón',             'mecanico2', '$2b$10$uGIkFpawK.rw/gbZbkuFEu5e6lMfxF/bh4ZbjekvQByu6TaV36uuu', 'mecanico',       TRUE),
    ('u_almacen',   'Rosa Injante',            'almacen',   '$2b$10$uGIkFpawK.rw/gbZbkuFEu5e6lMfxF/bh4ZbjekvQByu6TaV36uuu', 'almacen',        TRUE),
    ('u_jefe',      'Miguel Huamán',           'jefe',      '$2b$10$uGIkFpawK.rw/gbZbkuFEu5e6lMfxF/bh4ZbjekvQByu6TaV36uuu', 'jefe_taller',    TRUE),
    ('u_admin',     'Administrador MHESUS',    'admin',     '$2b$10$uGIkFpawK.rw/gbZbkuFEu5e6lMfxF/bh4ZbjekvQByu6TaV36uuu', 'administracion', TRUE);

INSERT INTO clientes (id, dni, nombres, apellidos, celular, direccion, creado_en) VALUES
    ('c_1', '42839112', 'Renato',   'Salcedo Díaz',    '956821034', 'Jr. Lima 245, Chincha Alta',        NOW()::text),
    ('c_2', '71982045', 'Milagros', 'Torres Vega',      '944210987', 'Av. Oscar R. Benavides 810',        NOW()::text),
    ('c_3', '46223190', 'Edwin',    'Cárdenas Ponce',   '987654321', 'Calle Los Álamos 112, Pueblo Nuevo', NOW()::text);

INSERT INTO motocicletas (id, cliente_id, placa, marca, modelo, anio, km_actual) VALUES
    ('m_1', 'c_1', 'MTL-812', 'Honda', 'CB160F',       2023, 8420),
    ('m_2', 'c_2', 'MTP-334', 'Bajaj', 'Pulsar NS200',  2022, 15230),
    ('m_3', 'c_3', 'MTQ-556', 'Honda', 'XR150L',        2021, 22110);

INSERT INTO productos (id, codigo, nombre, categoria, precio, stock_actual, stock_minimo) VALUES
    ('prod_1', 'ACE-10W40',  'Aceite motor 10W-40 (1L)',        'Lubricantes', 32,  40, 10),
    ('prod_2', 'FIL-AIR-01', 'Filtro de aire universal',        'Filtros',     18,  14, 5),
    ('prod_3', 'PAS-DEL-01', 'Pastillas de freno delanteras',   'Frenos',      45,  6,  8),
    ('prod_4', 'CAD-428H',   'Cadena de transmisión 428H',      'Transmisión', 95,  9,  4),
    ('prod_5', 'BUJ-STD',    'Bujía estándar',                  'Encendido',   12,  30, 10),
    ('prod_6', 'LLA-TRAS-01','Llanta trasera 100/90-17',        'Llantas',     180, 3,  3);
