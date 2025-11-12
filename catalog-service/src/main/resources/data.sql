-- Categorías base
INSERT INTO categoria (id_categoria, nombre) VALUES (1,'Zapatillas')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

INSERT INTO categoria (id_categoria, nombre) VALUES (2,'Poleras')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- Productos base (id compuesto: id_categoria + id_producto)
INSERT INTO producto (id_categoria, id_producto, marca, modelo, color, talla, precio, stock, image_url)
VALUES (1, 1001, 'Nike', 'Air Zoom', 'Negro', 'M', 59990, 25, 'https://example.com/img/air-zoom.jpg')
ON DUPLICATE KEY UPDATE marca=VALUES(marca), modelo=VALUES(modelo), color=VALUES(color), talla=VALUES(talla), precio=VALUES(precio), stock=VALUES(stock), image_url=VALUES(image_url);

INSERT INTO producto (id_categoria, id_producto, marca, modelo, color, talla, precio, stock, image_url)
VALUES (1, 1002, 'Adidas', 'Ultraboost', 'Blanco', 'L', 79990, 15, 'https://example.com/img/ultraboost.jpg')
ON DUPLICATE KEY UPDATE marca=VALUES(marca), modelo=VALUES(modelo), color=VALUES(color), talla=VALUES(talla), precio=VALUES(precio), stock=VALUES(stock), image_url=VALUES(image_url);

INSERT INTO producto (id_categoria, id_producto, marca, modelo, color, talla, precio, stock, image_url)
VALUES (2, 2001, 'Reebok', 'Basic Tee', 'Azul', 'S', 12990, 50, 'https://example.com/img/basic-tee.jpg')
ON DUPLICATE KEY UPDATE marca=VALUES(marca), modelo=VALUES(modelo), color=VALUES(color), talla=VALUES(talla), precio=VALUES(precio), stock=VALUES(stock), image_url=VALUES(image_url);

