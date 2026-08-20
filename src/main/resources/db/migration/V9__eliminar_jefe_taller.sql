-- ============================================================================
-- MHESUS — Migración: se elimina el rol "jefe_taller" del sistema por completo.
-- Control de calidad ahora lo cierra el mecánico solo al marcar el servicio
-- concluido, sin necesitar una aprobación separada de otro rol.
--
-- No se toca V1/V2 directamente porque Flyway ya las aplicó contra la base
-- real — se reasignan las referencias al usuario "jefe" antes de borrarlo,
-- para no dejar nada huérfano.
-- ============================================================================

UPDATE ordenes_trabajo SET asesor_id = (SELECT id FROM usuarios WHERE usuario = 'admin')
WHERE asesor_id = (SELECT id FROM usuarios WHERE usuario = 'jefe');

UPDATE ordenes_trabajo SET mecanico_id = NULL
WHERE mecanico_id = (SELECT id FROM usuarios WHERE usuario = 'jefe');

UPDATE movimientos_inventario SET usuario_id = (SELECT id FROM usuarios WHERE usuario = 'admin')
WHERE usuario_id = (SELECT id FROM usuarios WHERE usuario = 'jefe');

UPDATE auditoria SET usuario_id = (SELECT id FROM usuarios WHERE usuario = 'admin')
WHERE usuario_id = (SELECT id FROM usuarios WHERE usuario = 'jefe');

UPDATE notificaciones SET usuario_id = (SELECT id FROM usuarios WHERE usuario = 'admin')
WHERE usuario_id = (SELECT id FROM usuarios WHERE usuario = 'jefe');

UPDATE tareas SET creado_por = (SELECT id FROM usuarios WHERE usuario = 'admin')
WHERE creado_por = (SELECT id FROM usuarios WHERE usuario = 'jefe');

UPDATE tareas SET asignado_a = NULL
WHERE asignado_a = (SELECT id FROM usuarios WHERE usuario = 'jefe');

DELETE FROM usuarios WHERE usuario = 'jefe';
