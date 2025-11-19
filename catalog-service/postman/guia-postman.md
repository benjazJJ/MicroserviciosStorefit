# Guía Postman – Catalog Service

http://localhost:8081.


## Categorías

Las categorías son necesarias para crear productos. El ID de categoría (`idCategoria`) se genera automáticamente.

### 1) Listar categorías – GET

- URL: `http://localhost:8081/api/v1/categorias`
- Sirve para obtener todas las categorías.

### 2) Obtener categoría por ID – GET

- URL: `http://localhost:8081/api/v1/categorias/1`
- Sirve para obtener una categoría específica.

Responses:
- 200 OK: categoría encontrada.
- 404 Not Found: `{"message":"Categoría no encontrada: 1"}`

### 3) Crear categoría – POST

- URL: `http://localhost:8081/api/v1/categorias`
- Sirve para crear una categoría.
- Body:
```json
{
  "nombreCategoria": "Zapatillas"
}
```

Ejemplo response (201):
```json
{
  "message": "Categoría creada correctamente",
  "data": {
    "idCategoria": 1,
    "nombreCategoria": "Zapatillas"
  }
}
```
### 4) Actualizar categoría – PUT

- URL: `http://localhost:8081/api/v1/categorias/1`
- Sirve para actualizar el nombre de una categoría.
- Body:
```json
{
  "nombreCategoria": "Poleras"
}
```
Responses:
- 200 OK con mensaje: `"Categoría actualizada correctamente"` y `data` actualizada.
- 404 Not Found si no existe.

### 5) Eliminar categoría – DELETE

- URL: `http://localhost:8081/api/v1/categorias/1`
- Sirve para eliminar la categoría.

Responses:
- 200 OK: `{"message":"Categoría eliminada correctamente"}`
- 404 Not Found si no existe.

---

## Productos

Los productos tienen PK compuesta: `id.idCategoria` + `id.idProducto` (este último lo eliges tú, debe ser único dentro de la categoría).

### 1) Listar productos – GET

- URL: `http://localhost:8081/api/v1/productos`
- Sirve para obtener todos los productos.

Ejemplo response (200):
```json
[
  {
    "id": {"idCategoria": 1, "idProducto": 1001},
    "marca": "Adidas",
    "modelo": "Ultraboost 5",
    "color": "Negro",
    "talla": "M",
    "precio": 59990,
    "stock": 10,
    "imageUrl": "/img/test.png"
  }
]
```
### 2) Obtener producto por ID compuesto – GET

- URL: `http://localhost:8081/api/v1/productos/1/1001`
- Sirve para obtener un producto específico por categoría e id de producto.

Responses:
- 200 OK: devuelve el producto.
- 404 Not Found: `{"message":"Producto no encontrado: 1/1001"}`

### 3) Listar productos por categoría – GET

- URL: `http://localhost:8081/api/v1/productos/categoria/1`
- Sirve para listar productos de una categoría.

Response (200): array de productos (puede ser vacío).

### 4) Crear producto – POST

- URL: `http://localhost:8081/api/v1/productos`
- Sirve para crear un producto. Requiere que la categoría exista.
- Body (ejemplo):
```json
{
  "id": { "idCategoria": 1, "idProducto": 1001 },
  "marca": "Adidas",
  "modelo": "Ultraboost 5",
  "color": "Negro",
  "talla": "M",
  "precio": 59990,
  "stock": 10,
  "imageUrl": "/img/test.png"
}
```

Responses:
- 201 Created con Location `/api/v1/productos/1/1001` y body:
```json
{
  "message": "Producto añadido correctamente",
  "data": { /* producto creado */ }
}
```
- 404 si la categoría no existe: `{"message":"Categoría no existe: 1"}`
- 400 si el id ya existe o datos inválidos.

### 5) Actualizar producto – PUT

- URL: `{{baseUrl}}/api/v1/productos/1/1001`
- Sirve para actualizar datos del producto. No cambia la PK compuesta.
- Body (ejemplo – cambiando precio):
```json
{
  "id": { "idCategoria": 1, "idProducto": 1001 },
  "marca": "Adidas",
  "modelo": "Ultraboost 5",
  "color": "Negro",
  "talla": "M",
  "precio": 39990,
  "stock": 10,
  "imageUrl": "/img/test.png"
}
```

Responses:
- 200 OK: `{"message":"Producto actualizado correctamente","data":{...}}`
- 404 Not Found si no existe: `{"message":"Producto no encontrado: 1/1001"}`
- 400 Bad Request si datos inválidos.

### 6) Eliminar producto – DELETE

- URL: `{{baseUrl}}/api/v1/productos/1/1001`
- Sirve para eliminar un producto.

Responses:
- 200 OK: `{"message":"Producto eliminado correctamente"}`
- 404 Not Found si no existe: `{"message":"Producto no encontrado: 1/1001"}`

---

## Reserva de Stock

Endpoint para validar y descontar stock de múltiples productos en una sola operación.

### Reservar/descontar stock – POST

- URL: `{{baseUrl}}/api/v1/productos/stock/reservar`
- Body (ejemplo):
```json
[
  { "idProducto": 1001, "cantidad": 2 },
  { "idProducto": 2001, "cantidad": 1 }
]
```

Responses:
- 200 OK: `{"message":"Stock reservado correctamente"}`
- 400 Bad Request si lista vacía o cantidades <= 0: `{"message":"La lista de items de stock no puede estar vacía"}` o `{"message":"Cada item debe indicar idProducto y cantidad > 0"}`
- 400 Bad Request si stock insuficiente: `{"message":"Stock insuficiente para el producto 1001 (disponible=..., solicitado=...)"}`
- 404 Not Found si algún producto no existe: `{"message":"Producto no encontrado con id_producto=1001"}`

---

## Validaciones y mensajes de error

Manejo global de errores (códigos y mensajes JSON):

- 404 Not Found (`EntityNotFoundException`): `{"message":"... no encontrado ..."}`
- 400 Bad Request (`IllegalArgumentException`, validación `@Valid`): `{"message":"<detalle>"}`
- 400 Bad Request (`StockInsuficienteException`): `{"message":"Stock insuficiente ..."}`
- 500 Internal Server Error (otros): `{"message":"Ocurrió un error inesperado"}`

Archivo relevante: `src/main/java/com/storefit/catalog_service/Controller/GlobalExceptionHandler.java`.

---

## Sugerencias de prueba

1) Crea una categoría (POST) y guarda el `idCategoria`.
2) Crea un producto (POST) usando `idCategoria` y un `idProducto` (p. ej., 1001).
3) Lista productos (GET) y verifica el creado.
4) Obtén por ID compuesto (GET `/1/1001`).
5) Actualiza el producto (PUT) y valida cambios.
6) Prueba reserva de stock (POST `/stock/reservar`).
7) Elimina el producto (DELETE).
8) Fuerza errores 404/400 para validar mensajes.

