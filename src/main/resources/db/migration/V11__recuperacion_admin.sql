-- ============================================================================
-- MHESUS — Migración: recuperación de contraseña por correo, solo para
-- administradores (si el único admin se bloquea, no hay a quién avisarle
-- con el flujo normal de "olvidé mi contraseña").
-- ============================================================================

ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS email VARCHAR(200);

CREATE TABLE codigos_recuperacion (
    id         VARCHAR(64) PRIMARY KEY,
    usuario_id VARCHAR(64) NOT NULL REFERENCES usuarios(id),
    codigo     VARCHAR(6) NOT NULL,
    expira_en  VARCHAR(40) NOT NULL,
    usado      BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en  VARCHAR(40) NOT NULL
);
CREATE INDEX idx_codigos_recuperacion_usuario ON codigos_recuperacion(usuario_id, codigo);
