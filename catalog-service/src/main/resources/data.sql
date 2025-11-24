-- Catálogo base StoreFit
-- Categorías principales
INSERT INTO categoria (id_categoria, nombre) VALUES (1,'Poleras')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

INSERT INTO categoria (id_categoria, nombre) VALUES (2,'Poleron')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

INSERT INTO categoria (id_categoria, nombre) VALUES (3,'Buzo')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

INSERT INTO categoria (id_categoria, nombre) VALUES (4,'Conjunto Femenino')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- Productos base (dos colores por modelo, stock 80 c/u)
INSERT INTO producto (id_categoria, id_producto, marca, modelo, color, talla, precio, stock, image_url)
VALUES (1, 1001, 'StoreFit', 'XFITRX', 'Negro', 'M', 9990, 80, '/img/poleras/xfitrx_negro.png')
ON DUPLICATE KEY UPDATE marca=VALUES(marca), modelo=VALUES(modelo), color=VALUES(color), talla=VALUES(talla), precio=VALUES(precio), stock=VALUES(stock), image_url=VALUES(image_url);

INSERT INTO producto (id_categoria, id_producto, marca, modelo, color, talla, precio, stock, image_url)
VALUES (1, 1002, 'StoreFit', 'XFITRX', 'Blanco', 'L', 9990, 80, '/img/poleras/xfitrx_blanco.png')
ON DUPLICATE KEY UPDATE marca=VALUES(marca), modelo=VALUES(modelo), color=VALUES(color), talla=VALUES(talla), precio=VALUES(precio), stock=VALUES(stock), image_url=VALUES(image_url);

INSERT INTO producto (id_categoria, id_producto, marca, modelo, color, talla, precio, stock, image_url)
VALUES (2, 2001, 'StoreFit', 'WARMGLIDE', 'Negro', 'L', 17990, 80, '/img/poleron/warmglide_negro.png')
ON DUPLICATE KEY UPDATE marca=VALUES(marca), modelo=VALUES(modelo), color=VALUES(color), talla=VALUES(talla), precio=VALUES(precio), stock=VALUES(stock), image_url=VALUES(image_url);

INSERT INTO producto (id_categoria, id_producto, marca, modelo, color, talla, precio, stock, image_url)
VALUES (2, 2002, 'StoreFit', 'WARMGLIDE', 'Blanco', 'M', 17990, 80, '/img/poleron/warmglide_blanco.png')
ON DUPLICATE KEY UPDATE marca=VALUES(marca), modelo=VALUES(modelo), color=VALUES(color), talla=VALUES(talla), precio=VALUES(precio), stock=VALUES(stock), image_url=VALUES(image_url);

INSERT INTO producto (id_categoria, id_producto, marca, modelo, color, talla, precio, stock, image_url)
VALUES (3, 3001, 'StoreFit', 'FLEXRUN', 'Negro', 'M', 14990, 80, '/img/buzo/flexrun_negro.png')
ON DUPLICATE KEY UPDATE marca=VALUES(marca), modelo=VALUES(modelo), color=VALUES(color), talla=VALUES(talla), precio=VALUES(precio), stock=VALUES(stock), image_url=VALUES(image_url);

INSERT INTO producto (id_categoria, id_producto, marca, modelo, color, talla, precio, stock, image_url)
VALUES (3, 3002, 'StoreFit', 'FLEXRUN', 'Blanco', 'S', 14990, 80, '/img/buzo/flexrun_blanco.png')
ON DUPLICATE KEY UPDATE marca=VALUES(marca), modelo=VALUES(modelo), color=VALUES(color), talla=VALUES(talla), precio=VALUES(precio), stock=VALUES(stock), image_url=VALUES(image_url);

INSERT INTO producto (id_categoria, id_producto, marca, modelo, color, talla, precio, stock, image_url)
VALUES (4, 4001, 'StoreFit', 'FITQUEEN', 'Negro', 'M', 19990, 80, '/img/conjunto/fitqueen_negro.png')
ON DUPLICATE KEY UPDATE marca=VALUES(marca), modelo=VALUES(modelo), color=VALUES(color), talla=VALUES(talla), precio=VALUES(precio), stock=VALUES(stock), image_url=VALUES(image_url);

INSERT INTO producto (id_categoria, id_producto, marca, modelo, color, talla, precio, stock, image_url)
VALUES (4, 4002, 'StoreFit', 'FITQUEEN', 'Blanco', 'S', 19990, 80, '/img/conjunto/fitqueen_blanco.png')
ON DUPLICATE KEY UPDATE marca=VALUES(marca), modelo=VALUES(modelo), color=VALUES(color), talla=VALUES(talla), precio=VALUES(precio), stock=VALUES(stock), image_url=VALUES(image_url);
