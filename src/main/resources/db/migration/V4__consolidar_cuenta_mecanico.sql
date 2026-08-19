-- ============================================================================
-- MHESUS — Migración: consolidar las cuentas de mecánico en una sola compartida.
-- Antes había dos logins distintos (mecanico / mecanico2); ahora todos los
-- mecánicos del taller entran con el mismo usuario ("mecanico"). Quién recibe
-- cada OT se ve dentro del sistema (columna "Mecánico asignado"), no por tener
-- una cuenta de login distinta cada uno.
--
-- No se toca V2__seed_data.sql directamente porque Flyway ya la aplicó contra
-- la base real — las migraciones ya corridas no se editan, se agregan nuevas.
-- ============================================================================

-- Si alguna OT de prueba quedó asignada a mecanico2, la reasignamos a la
-- cuenta compartida antes de borrar el usuario, para no dejar una referencia
-- huérfana en ordenes_trabajo.mecanico_id.
UPDATE ordenes_trabajo
SET mecanico_id = (SELECT id FROM usuarios WHERE usuario = 'mecanico')
WHERE mecanico_id = (SELECT id FROM usuarios WHERE usuario = 'mecanico2');

UPDATE movimientos_inventario
SET usuario_id = (SELECT id FROM usuarios WHERE usuario = 'mecanico')
WHERE usuario_id = (SELECT id FROM usuarios WHERE usuario = 'mecanico2');

UPDATE auditoria
SET usuario_id = (SELECT id FROM usuarios WHERE usuario = 'mecanico')
WHERE usuario_id = (SELECT id FROM usuarios WHERE usuario = 'mecanico2');

UPDATE notificaciones
SET usuario_id = (SELECT id FROM usuarios WHERE usuario = 'mecanico')
WHERE usuario_id = (SELECT id FROM usuarios WHERE usuario = 'mecanico2');

DELETE FROM usuarios WHERE usuario = 'mecanico2';

UPDATE usuarios SET nombre = 'Mecánica MHESUS' WHERE usuario = 'mecanico';
