-- Asegura IDs exactos: 1=CLIENTE, 2=ADMIN, 3=SOPORTE

DELETE FROM rol WHERE nombre_rol='CLIENTE' AND rol_id<>1;
DELETE FROM rol WHERE nombre_rol='ADMIN'   AND rol_id<>2;
DELETE FROM rol WHERE nombre_rol='SOPORTE' AND rol_id<>3;

INSERT INTO rol (rol_id, nombre_rol) VALUES (1,'CLIENTE')
ON DUPLICATE KEY UPDATE nombre_rol=VALUES(nombre_rol);

INSERT INTO rol (rol_id, nombre_rol) VALUES (2,'ADMIN')
ON DUPLICATE KEY UPDATE nombre_rol=VALUES(nombre_rol);

INSERT INTO rol (rol_id, nombre_rol) VALUES (3,'SOPORTE')
ON DUPLICATE KEY UPDATE nombre_rol=VALUES(nombre_rol);

