# API Tienda

API REST construida con Spring Boot y PostgreSQL que expone el CRUD completo de **Productos** y un módulo de **Pedidos** que funciona como carrito de compras: permite agregar y retirar productos, calcula automáticamente subtotales y total, y descuenta el inventario al confirmar la compra.

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java (JDK) | 25 |
| Spring Boot | 4.1.1 (Web MVC, Data JPA, Validation) |
| Hibernate ORM | 7.4 |
| Lombok | Gestionado por Spring Boot |
| PostgreSQL | 15 o superior (probado con 18) |
| Maven | Wrapper incluido (`mvnw` / `mvnw.cmd`) |

## Puesta en marcha

### 1. Base de datos

Con PostgreSQL instalado y el servicio en ejecución en el puerto **5432**, crear la base desde pgAdmin o psql:

```sql
CREATE DATABASE tienda_db;
```

Alternativa con Docker: `docker compose up -d` levanta PostgreSQL con la base ya creada.

### 2. Conexión

La configuración está en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/tienda_db
spring.datasource.username=postgres
spring.datasource.password={password}
```

### 3. Compilar y ejecutar

```powershell
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
```

En Linux/macOS usar `./mvnw`. La API queda disponible en `http://localhost:8080` y Hibernate crea las tablas automáticamente (`ddl-auto=update`).

> Si el IDE marca errores como `cannot find symbol: method getNombre()`, instalar el plugin de Lombok y habilitar *Annotation Processing*.

## Estructura del proyecto

```
com.tienda.api
├── controller   ProductoController, PedidoController
├── service      ProductoService, PedidoService
├── repository   ProductoRepository, PedidoRepository
├── model        Producto, Pedido, DetallePedido, EstadoPedido
├── dto          PedidoRequest, ItemRequest
└── exception    RecursoNoEncontradoException, ManejadorGlobalExcepciones
```

## Modelo de datos

| Tabla | Columnas principales | Relación |
|---|---|---|
| `productos` | id, nombre, descripcion, precio, stock | Un producto puede aparecer en muchos detalles |
| `pedidos` | id, cliente, fecha, estado, total | Un pedido tiene muchos detalles (1:N) |
| `detalle_pedidos` | id, pedido_id, producto_id, cantidad, precio_unitario, subtotal | Cada detalle pertenece a un pedido y a un producto (N:1) |

Los valores monetarios usan `BigDecimal` / `numeric(12,2)`. El estado del pedido es `CARRITO`, `CONFIRMADO` o `CANCELADO`.

### Reglas de negocio del carrito

1. El subtotal de cada ítem es cantidad × precio unitario y el total es la suma de los subtotales. Ambos se calculan en el servidor.
2. La cantidad de cada ítem debe ser mayor que cero.
3. Solo los pedidos en estado `CARRITO` pueden modificarse (actualizar, agregar o retirar ítems, confirmar).
4. Al confirmar se valida el stock de **todos** los productos antes de descontar; si alguno no alcanza, no se modifica ningún stock.
5. No se puede eliminar un producto que forme parte de un pedido (409 Conflict).

## Endpoints

Todas las respuestas de error tienen el formato `{"error": "mensaje"}`.

### Productos

| Método | Ruta | Descripción | Códigos |
|---|---|---|---|
| GET | `/api/productos` | Listar todos los productos | 200 |
| GET | `/api/productos/{id}` | Consultar un producto | 200 / 404 |
| POST | `/api/productos` | Crear un producto | 201 / 400 |
| PUT | `/api/productos/{id}` | Actualizar un producto | 200 / 400 / 404 |
| DELETE | `/api/productos/{id}` | Eliminar un producto | 204 / 404 / 409 |

Petición `POST` / `PUT /api/productos`:

```json
{
  "nombre": "Teclado mecánico",
  "descripcion": "Switches rojos, retroiluminado",
  "precio": 189900.00,
  "stock": 15
}
```

Respuesta `201 Created`:

```json
{
  "id": 1,
  "nombre": "Teclado mecánico",
  "descripcion": "Switches rojos, retroiluminado",
  "precio": 189900.00,
  "stock": 15
}
```

### Pedidos

| Método | Ruta | Descripción | Códigos |
|---|---|---|---|
| GET | `/api/pedidos` | Listar todos los pedidos | 200 |
| GET | `/api/pedidos/{id}` | Consultar un pedido con su detalle | 200 / 404 |
| POST | `/api/pedidos` | Crear un pedido (carrito) con sus ítems | 201 / 400 / 404 |
| PUT | `/api/pedidos/{id}` | Actualizar el cliente y los ítems del carrito | 200 / 400 / 404 |
| DELETE | `/api/pedidos/{id}` | Eliminar un pedido y su detalle | 204 / 404 |
| POST | `/api/pedidos/{id}/items` | Agregar un producto al carrito | 200 / 400 / 404 |
| DELETE | `/api/pedidos/{id}/items/{itemId}` | Retirar un producto del carrito | 200 / 400 / 404 |
| PATCH | `/api/pedidos/{id}/confirmar` | Confirmar la compra y descontar inventario | 200 / 400 / 404 |

Petición `POST` / `PUT /api/pedidos`:

```json
{
  "cliente": "Laura Gómez",
  "items": [
    { "productoId": 1, "cantidad": 2 },
    { "productoId": 3, "cantidad": 1 }
  ]
}
```

Petición `POST /api/pedidos/{id}/items`:

```json
{ "productoId": 2, "cantidad": 4 }
```

Respuesta `201 Created` de un pedido:

```json
{
  "id": 1,
  "cliente": "Laura Gómez",
  "fecha": "2026-10-20T21:18:48.523484",
  "estado": "CARRITO",
  "total": 1029700.00,
  "items": [
    {
      "id": 1,
      "producto": { "id": 1, "nombre": "Teclado mecánico", "descripcion": "Switches rojos, retroiluminado", "precio": 189900.00, "stock": 15 },
      "cantidad": 2,
      "precioUnitario": 189900.00,
      "subtotal": 379800.00
    },
    {
      "id": 2,
      "producto": { "id": 3, "nombre": "Monitor 24", "descripcion": "IPS 75 Hz", "precio": 649900.00, "stock": 2 },
      "cantidad": 1,
      "precioUnitario": 649900.00,
      "subtotal": 649900.00
    }
  ]
}
```

Ejemplos de error:

```json
// 400 — PATCH /api/pedidos/2/confirmar sin inventario suficiente
{ "error": "Stock insuficiente para Monitor 24 (disponible: 2, solicitado: 5)" }

// 409 — DELETE /api/productos/1 cuando el producto está en un pedido
{ "error": "El producto está asociado a uno o más pedidos" }
```

## Pruebas

La colección `postman/API-Tienda.postman_collection.json` contiene los casos de prueba P1–P10 (Productos) y C1–C15 (Pedidos). Importarla en Postman y ejecutarla en orden sobre una base de datos vacía.

## Autor

- Brygsnny Rojas
