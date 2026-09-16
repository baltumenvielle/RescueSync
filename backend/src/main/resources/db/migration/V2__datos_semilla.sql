-- Datos semilla para desarrollo y demo.
-- Contraseña de todos los usuarios: rescuesync  (BCrypt, cost 10)
-- bonita_username coincide con username: cada usuario debe tener su usuario espejo en la
-- organización de Bonita con la misma contraseña (ver docs/decisiones.md).

INSERT INTO organizacion (id, tipo, nombre, cuit, contacto) VALUES
    (1, 'MUNICIPIO', 'Municipalidad de La Plata',            '30-99900001-1', 'emergencias@laplata.gob.ar'),
    (2, 'MUNICIPIO', 'Municipalidad de Berisso',             '30-99900002-2', 'defensacivil@berisso.gob.ar'),
    (3, 'CCR',       'Centro Coordinador Regional Capital',  '30-99900003-3', 'guardia@ccr-capital.gob.ar'),
    (4, 'ONG',       'Cruz Roja Argentina - Filial La Plata', '30-99900004-4', 'laplata@cruzroja.org.ar'),
    (5, 'ONG',       'Cáritas La Plata',                     '30-99900005-5', 'contacto@caritaslaplata.org.ar'),
    (6, 'ONG',       'Bomberos Voluntarios de Ensenada',     '30-99900006-6', 'bomberos@ensenada.org.ar'),
    (7, 'AUDITORIA', 'Dirección Provincial de Auditoría',    '30-99900007-7', 'auditoria@gba.gob.ar');

SELECT setval('organizacion_id_seq', (SELECT MAX(id) FROM organizacion));

INSERT INTO usuario (username, password_hash, email, nombre, rol, organizacion_id, bonita_username) VALUES
    ('municipio.laplata', '$2a$10$Zi7iqrBVSb95PBz4ztgAduy/bxxByab/.e4s088sPhbltAOuiExWa', 'operador@laplata.gob.ar',   'Laura Operadora',  'OPERADOR_MUNICIPAL', 1, 'municipio.laplata'),
    ('municipio.berisso', '$2a$10$Zi7iqrBVSb95PBz4ztgAduy/bxxByab/.e4s088sPhbltAOuiExWa', 'operador@berisso.gob.ar',   'Bruno Operador',   'OPERADOR_MUNICIPAL', 2, 'municipio.berisso'),
    ('ccr',               '$2a$10$Zi7iqrBVSb95PBz4ztgAduy/bxxByab/.e4s088sPhbltAOuiExWa', 'coordinacion@ccr.gob.ar',   'Carla Coordinadora','CCR',               3, 'ccr'),
    ('ong.cruzroja',      '$2a$10$Zi7iqrBVSb95PBz4ztgAduy/bxxByab/.e4s088sPhbltAOuiExWa', 'ofertas@cruzroja.org.ar',   'Rocío Cruz Roja',  'REPRESENTANTE_ONG',  4, 'ong.cruzroja'),
    ('ong.caritas',       '$2a$10$Zi7iqrBVSb95PBz4ztgAduy/bxxByab/.e4s088sPhbltAOuiExWa', 'ofertas@caritas.org.ar',    'Tomás Cáritas',    'REPRESENTANTE_ONG',  5, 'ong.caritas'),
    ('ong.bomberos',      '$2a$10$Zi7iqrBVSb95PBz4ztgAduy/bxxByab/.e4s088sPhbltAOuiExWa', 'ofertas@bomberos.org.ar',   'Esteban Bombero',  'REPRESENTANTE_ONG',  6, 'ong.bomberos'),
    ('auditor',           '$2a$10$Zi7iqrBVSb95PBz4ztgAduy/bxxByab/.e4s088sPhbltAOuiExWa', 'auditor@gba.gob.ar',        'Ana Auditora',     'AUDITOR',            7, NULL);
