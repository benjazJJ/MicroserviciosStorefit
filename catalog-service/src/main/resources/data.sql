-- Catalogo base StoreFit
-- Categorias principales
INSERT INTO categoria (id_categoria, nombre) VALUES (1,'Poleras')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

INSERT INTO categoria (id_categoria, nombre) VALUES (2,'Poleron')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

INSERT INTO categoria (id_categoria, nombre) VALUES (3,'Buzo')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

INSERT INTO categoria (id_categoria, nombre) VALUES (4,'Conjunto Femenino')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- Productos con stock uniforme por talla (XS a XL) para colores Blanco y Negro
-- Poleras (categoria 1) - modelo XFITRX
INSERT INTO producto (id_categoria, id_producto, marca, modelo, color, talla, precio, stock, image_url) VALUES
  (1, 1001, 'StoreFit', 'XFITRX', 'Negro', 'XS', 9990, 80, '/img/poleras/xfitrx_negro.png'),
  (1, 1002, 'StoreFit', 'XFITRX', 'Negro', 'S',  9990, 80, '/img/poleras/xfitrx_negro.png'),
  (1, 1003, 'StoreFit', 'XFITRX', 'Negro', 'M',  9990, 80, '/img/poleras/xfitrx_negro.png'),
  (1, 1004, 'StoreFit', 'XFITRX', 'Negro', 'L',  9990, 80, '/img/poleras/xfitrx_negro.png'),
  (1, 1005, 'StoreFit', 'XFITRX', 'Negro', 'XL', 9990, 80, '/img/poleras/xfitrx_negro.png'),
  (1, 1006, 'StoreFit', 'XFITRX', 'Blanco','XS', 9990, 80, '/img/poleras/xfitrx_blanco.png'),
  (1, 1007, 'StoreFit', 'XFITRX', 'Blanco','S',  9990, 80, '/img/poleras/xfitrx_blanco.png'),
  (1, 1008, 'StoreFit', 'XFITRX', 'Blanco','M',  9990, 80, '/img/poleras/xfitrx_blanco.png'),
  (1, 1009, 'StoreFit', 'XFITRX', 'Blanco','L',  9990, 80, '/img/poleras/xfitrx_blanco.png'),
  (1, 1010, 'StoreFit', 'XFITRX', 'Blanco','XL', 9990, 80, '/img/poleras/xfitrx_blanco.png')
ON DUPLICATE KEY UPDATE marca=VALUES(marca), modelo=VALUES(modelo), color=VALUES(color), talla=VALUES(talla), precio=VALUES(precio), stock=VALUES(stock), image_url=VALUES(image_url);

-- Poleron (categoria 2) - modelo WARMGLIDE
INSERT INTO producto (id_categoria, id_producto, marca, modelo, color, talla, precio, stock, image_url) VALUES
  (2, 2001, 'StoreFit', 'WARMGLIDE', 'Negro', 'XS', 17990, 80, '/img/poleron/warmglide_negro.png'),
  (2, 2002, 'StoreFit', 'WARMGLIDE', 'Negro', 'S',  17990, 80, '/img/poleron/warmglide_negro.png'),
  (2, 2003, 'StoreFit', 'WARMGLIDE', 'Negro', 'M',  17990, 80, '/img/poleron/warmglide_negro.png'),
  (2, 2004, 'StoreFit', 'WARMGLIDE', 'Negro', 'L',  17990, 80, '/img/poleron/warmglide_negro.png'),
  (2, 2005, 'StoreFit', 'WARMGLIDE', 'Negro', 'XL', 17990, 80, '/img/poleron/warmglide_negro.png'),
  (2, 2006, 'StoreFit', 'WARMGLIDE', 'Blanco','XS', 17990, 80, '/img/poleron/warmglide_blanco.png'),
  (2, 2007, 'StoreFit', 'WARMGLIDE', 'Blanco','S',  17990, 80, '/img/poleron/warmglide_blanco.png'),
  (2, 2008, 'StoreFit', 'WARMGLIDE', 'Blanco','M',  17990, 80, '/img/poleron/warmglide_blanco.png'),
  (2, 2009, 'StoreFit', 'WARMGLIDE', 'Blanco','L',  17990, 80, '/img/poleron/warmglide_blanco.png'),
  (2, 2010, 'StoreFit', 'WARMGLIDE', 'Blanco','XL', 17990, 80, '/img/poleron/warmglide_blanco.png')
ON DUPLICATE KEY UPDATE marca=VALUES(marca), modelo=VALUES(modelo), color=VALUES(color), talla=VALUES(talla), precio=VALUES(precio), stock=VALUES(stock), image_url=VALUES(image_url);

-- Buzo (categoria 3) - modelo FLEXRUN
INSERT INTO producto (id_categoria, id_producto, marca, modelo, color, talla, precio, stock, image_url) VALUES
  (3, 3001, 'StoreFit', 'FLEXRUN', 'Negro', 'XS', 14990, 80, '/img/buzo/flexrun_negro.png'),
  (3, 3002, 'StoreFit', 'FLEXRUN', 'Negro', 'S',  14990, 80, '/img/buzo/flexrun_negro.png'),
  (3, 3003, 'StoreFit', 'FLEXRUN', 'Negro', 'M',  14990, 80, '/img/buzo/flexrun_negro.png'),
  (3, 3004, 'StoreFit', 'FLEXRUN', 'Negro', 'L',  14990, 80, '/img/buzo/flexrun_negro.png'),
  (3, 3005, 'StoreFit', 'FLEXRUN', 'Negro', 'XL', 14990, 80, '/img/buzo/flexrun_negro.png'),
  (3, 3006, 'StoreFit', 'FLEXRUN', 'Blanco','XS', 14990, 80, '/img/buzo/flexrun_blanco.png'),
  (3, 3007, 'StoreFit', 'FLEXRUN', 'Blanco','S',  14990, 80, '/img/buzo/flexrun_blanco.png'),
  (3, 3008, 'StoreFit', 'FLEXRUN', 'Blanco','M',  14990, 80, '/img/buzo/flexrun_blanco.png'),
  (3, 3009, 'StoreFit', 'FLEXRUN', 'Blanco','L',  14990, 80, '/img/buzo/flexrun_blanco.png'),
  (3, 3010, 'StoreFit', 'FLEXRUN', 'Blanco','XL', 14990, 80, '/img/buzo/flexrun_blanco.png')
ON DUPLICATE KEY UPDATE marca=VALUES(marca), modelo=VALUES(modelo), color=VALUES(color), talla=VALUES(talla), precio=VALUES(precio), stock=VALUES(stock), image_url=VALUES(image_url);

-- Conjunto Femenino (categoria 4) - modelo FITQUEEN
INSERT INTO producto (id_categoria, id_producto, marca, modelo, color, talla, precio, stock, image_url) VALUES
  (4, 4001, 'StoreFit', 'FITQUEEN', 'Negro', 'XS', 19990, 80, '/img/conjunto/fitqueen_negro.png'),
  (4, 4002, 'StoreFit', 'FITQUEEN', 'Negro', 'S',  19990, 80, '/img/conjunto/fitqueen_negro.png'),
  (4, 4003, 'StoreFit', 'FITQUEEN', 'Negro', 'M',  19990, 80, '/img/conjunto/fitqueen_negro.png'),
  (4, 4004, 'StoreFit', 'FITQUEEN', 'Negro', 'L',  19990, 80, '/img/conjunto/fitqueen_negro.png'),
  (4, 4005, 'StoreFit', 'FITQUEEN', 'Negro', 'XL', 19990, 80, '/img/conjunto/fitqueen_negro.png'),
  (4, 4006, 'StoreFit', 'FITQUEEN', 'Blanco','XS', 19990, 80, '/img/conjunto/fitqueen_blanco.png'),
  (4, 4007, 'StoreFit', 'FITQUEEN', 'Blanco','S',  19990, 80, '/img/conjunto/fitqueen_blanco.png'),
  (4, 4008, 'StoreFit', 'FITQUEEN', 'Blanco','M',  19990, 80, '/img/conjunto/fitqueen_blanco.png'),
  (4, 4009, 'StoreFit', 'FITQUEEN', 'Blanco','L',  19990, 80, '/img/conjunto/fitqueen_blanco.png'),
  (4, 4010, 'StoreFit', 'FITQUEEN', 'Blanco','XL', 19990, 80, '/img/conjunto/fitqueen_blanco.png')
ON DUPLICATE KEY UPDATE marca=VALUES(marca), modelo=VALUES(modelo), color=VALUES(color), talla=VALUES(talla), precio=VALUES(precio), stock=VALUES(stock), image_url=VALUES(image_url);