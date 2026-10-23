# Ecommerce API

API REST para una plataforma de comercio electrónico, construida con Java 21,
Spring Boot, Spring Web MVC, Spring Data JPA, Spring Security y PostgreSQL.

> **Estado actual:** el catálogo público, la autenticación con JWT, la gestión de
> usuarios y la taxonomía (categorías y marcas) están expuestos por HTTP. Los
> módulos de carrito, pedidos, reseñas, ajustes y contenido del sitio están
> pendientes.

## Contenido

- [Arquitectura](#arquitectura)
- [Requisitos](#requisitos)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [Contrato HTTP](#contrato-http)
- [Seguridad](#seguridad)
- [Errores](#errores)
- [Reglas de catálogo](#reglas-de-catálogo)
- [Pruebas](#pruebas)
- [Siguiente nivel para producción](#siguiente-nivel-para-producción)

## Arquitectura

El código está organizado por dominio y responsabilidad:

```text
com.david.ecommerce_api
├── auth/                 # autenticación (en construcción)
├── product/
│   ├── domain/           # entidades y reglas de negocio
│   ├── repository/       # persistencia y Specifications
│   ├── service/          # casos de uso
│   └── web/              # controllers, DTOs y mapeadores
├── common/
│   ├── dto/              # respuestas reutilizables
│   ├── error/            # errores y handler global
│   ├── persistence/      # auditoría/base de entidades
│   └── validation/       # validaciones de dominio
└── config/               # seguridad, CORS, locale y JPA
```

El flujo recomendado para una petición es:

```text
Controller → Service → Repository → PostgreSQL
     ↓          ↓
   DTO       reglas de dominio
```

Los controllers no exponen entidades JPA directamente: el catálogo usa
`ProductResponses` como DTO de salida y `ProductWebMapper` para transformar el
modelo de dominio en respuestas HTTP.

## Requisitos

- Java 21
- Maven Wrapper incluido (`mvnw.cmd`)
- PostgreSQL
- Base de datos `ecommerce_db`

## Configuración

La configuración base está en
`src/main/resources/application.properties`. Antes de ejecutar la aplicación,
configura la conexión de PostgreSQL y **no almacenes credenciales reales en
Git**. En entornos profesionales, sobrescribe estos valores mediante variables
de entorno o un gestor de secretos.

Ejemplo de variables para desarrollo:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/ecommerce_db"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "<tu-password-local>"
```

La aplicación escucha en `http://localhost:8080`.

## Ejecución

Desde la raíz del proyecto:

```powershell
.\mvnw.cmd spring-boot:run
```

La compilación y el empaquetado se pueden ejecutar con:

```powershell
.\mvnw.cmd clean verify
```

## Contrato HTTP

### Convenciones

- Base URL local: `http://localhost:8080`
- Formato de respuesta: JSON
- Las páginas son indexadas desde `0`
- `size` está limitado por endpoint para evitar respuestas excesivamente
  grandes
- Los slugs identifican productos publicados

### Listar productos

```http
GET /api/public/products
```

Parámetros opcionales:

| Parámetro | Tipo | Predeterminado | Descripción |
|---|---:|---:|---|
| `q` | string | — | Busca en nombre y descripción corta |
| `onSale` | boolean | — | Filtra productos en oferta |
| `sort` | string | `latest` | `latest`, `price-asc`, `price-desc`, `rating`, `top-selling` |
| `page` | integer | `0` | Página, mínimo `0` |
| `size` | integer | `12` | Elementos, entre `1` y `60` |

Ejemplo:

```bash
curl "http://localhost:8080/api/public/products?q=shirt&onSale=true&sort=price-asc&page=0&size=12"
```

La respuesta usa el formato:

```json
{
  "content": [
    {
      "id": 1,
      "slug": "classic-shirt",
      "name": "Classic Shirt",
      "thumbnailUrl": "https://cdn.example.com/shirt.jpg",
      "thumbnailAlt": "Classic Shirt",
      "price": 39.99,
      "compareAtPrice": 49.99,
      "discountPercent": 20,
      "ratingAverage": 4.5,
      "ratingCount": 12,
      "onSale": true,
      "newArrival": false,
      "topSelling": false
    }
  ],
  "page": 0,
  "size": 12,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### Nuevos lanzamientos

```http
GET /api/public/products/new-arrivals?page=0&size=12
```

Devuelve productos publicados durante los últimos 30 días, ordenados por fecha
de publicación descendente.

### Más vendidos

```http
GET /api/public/products/top-selling?page=0&size=12
```

Devuelve productos publicados con al menos una venta, ordenados por ventas
totales, valoración e identificador.

### Productos en oferta

```http
GET /api/public/products/on-sale?sort=price-desc&page=0&size=12
```

Un producto está en oferta cuando `compareAtPrice` es mayor que `price`.

### Detalle de producto

```http
GET /api/public/products/{slug}
```

Devuelve información comercial, galería, colores, tallas, variantes, stock
disponible, descripción, características y preguntas frecuentes. Solo devuelve
productos con estado `PUBLISHED`.

### Reseñas aprobadas

```http
GET /api/public/products/{slug}/reviews?sort=latest&page=0&size=6
```

`sort` admite `latest` y `oldest`; `size` debe estar entre `1` y `50`.
Solo se incluyen reseñas aprobadas de productos publicados.

### Catálogo completo para la tienda

```http
GET /api/public/catalog
GET /api/public/catalog/{id}
```

Devuelven los productos publicados con todo lo que la tienda necesita en un solo
documento (marca, categoría, estilo, colores, tallas, stock, descripción y
preguntas), para que el front filtre, ordene y pagine por su cuenta. Pensado
para catálogos de unos cientos de productos.

- `price` es el precio normal y `discountPrice` el precio con descuento (o
  `null`); `discount` es el porcentaje. En la base de datos el precio actual es
  `price` y el normal es `compareAtPrice`.
- `image` es la imagen principal e `images` las demás.
- `stock` es la suma del stock de las variantes activas.

### Categorías y marcas

```http
GET /api/public/categories        # solo las visibles
GET /api/public/brands            # solo las visibles
```

Respuesta de una categoría: `id`, `name`, `slug`, `description`, `image`,
`visible`. Una marca devuelve `id`, `name`, `slug`, `logo`, `visible`.

### Autenticación

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/auth/register` | público | Crea una cuenta de cliente y devuelve `{ token, user }` (201) |
| POST | `/api/auth/login` | público | Devuelve `{ token, user }`; 401 con credenciales inválidas, 403 si la cuenta está bloqueada |
| GET | `/api/auth/me` | autenticado | Usuario de la sesión |
| PUT | `/api/auth/profile` | autenticado | Cambia nombre y email (409 si el email ya existe) |
| PUT | `/api/auth/password` | autenticado | Cambia la contraseña con `{ current, next }` (204) |

El token se envía en cada petición con `Authorization: Bearer <token>`. El
usuario devuelto tiene `id`, `name`, `email`, `role` (`admin` o `customer`),
`blocked` y `createdAt` (milisegundos); nunca incluye la contraseña.

### Administración de productos

Todas bajo `/api/admin/products` (rol `ADMIN`):

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/admin/products?q=&status=&page=&size=` | Lista paginada (todos los estados) con búsqueda por nombre o slug |
| GET | `/api/admin/products/{id}` | Producto completo para editar |
| POST | `/api/admin/products` | Crea un borrador (201) |
| PUT | `/api/admin/products/{id}` | Reemplaza el contenido del producto |
| POST | `/api/admin/products/{id}/publish`, `unpublish`, `archive`, `restore` | Cambios de estado |
| DELETE | `/api/admin/products/{id}` | Borra un producto no publicado (409 si está publicado) |

El cuerpo de `POST` y `PUT` describe el producto entero: `name`, `slug`
(opcional), `shortDescription`, `price`, `compareAtPrice`, `categoryId`,
`brandId`, `dressStyle`, `images` (`imageUrl`, `publicId` opcional, `alt`,
`main`), `colors` (`name`, `hex`), `sizes` (`name`, `value`), `variants`
(`color`, `size`, `sku`, `stock`, `priceOverride`, `active`), `detail`,
`features` y `faqs`.

- Colores, tallas y variantes se identifican por nombre, valor y pareja
  color/talla: se actualizan y **no cambian de id**. Los que ya no aparecen en el
  cuerpo se desactivan (no se borran) y dejan de verse en la tienda.
- Imágenes, características y preguntas frecuentes se reemplazan por completo.
- Si una imagen no trae `publicId`, se usa su URL.
- Publicar exige al menos una imagen y una variante activa (409 si no).
- Un SKU repetido entre productos responde 409 (`DATA_CONFLICT`).

### Administración de usuarios, categorías y marcas

Todas las rutas `/api/admin/**` exigen el rol `ADMIN`.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/admin/users` | Lista de usuarios |
| PATCH | `/api/admin/users/{id}/blocked` | Bloquea o desbloquea con `{ "blocked": true }`; un administrador no se puede bloquear |
| GET, POST | `/api/admin/categories` | Lista todas (visibles o no) y crea |
| GET, PUT, DELETE | `/api/admin/categories/{id}` | Consulta, edita y borra |
| GET, POST | `/api/admin/brands` | Igual que categorías |
| GET, PUT, DELETE | `/api/admin/brands/{id}` | Igual que categorías |

Si no se envía `slug` se genera a partir del nombre; un slug repetido responde
409 (`TAXONOMY_SLUG_TAKEN`). `visible` es `true` por omisión.

## Seguridad

La configuración está en
`src/main/java/com/david/ecommerce_api/config/SecurityConfig.java`:

- Sesión sin estado: cada petición se autentica con un JWT firmado (HS256).
- `/api/public/**`, `/api/auth/login` y `/api/auth/register` son públicos.
- `/api/admin/**` exige el rol `ADMIN`; el resto exige autenticación.
- Si el usuario está bloqueado o ya no existe, el token deja de valer.
- Las contraseñas se guardan con BCrypt.
- CSRF está deshabilitado porque no se usan cookies de sesión.
- CORS permite los orígenes de `CORS_ALLOWED_ORIGINS` (por defecto
  `http://localhost:3000`).

Variables de entorno nuevas:

| Variable | Descripción |
|---|---|
| `JWT_SECRET` | Clave de firma, mínimo 32 caracteres (obligatoria) |
| `JWT_EXPIRATION_HOURS` | Vida del token, 24 por defecto |
| `CORS_ALLOWED_ORIGINS` | Orígenes separados por comas |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NAME` | Crean el primer administrador al arrancar si no existe; si están vacías no se crea ninguno |

## Errores

Los errores de dominio y validación se normalizan mediante
`GlobalExceptionHandler`:

```json
{
  "timestamp": "2026-08-21T04:00:00Z",
  "status": 404,
  "error": "Not Found",
  "code": "RESOURCE_NOT_FOUND",
  "message": "Resource was not found: product",
  "path": "/api/public/products/unknown",
  "fields": []
}
```

Las validaciones de parámetros incluyen `fields` con el nombre del campo y el
mensaje asociado. Los mensajes se internacionalizan mediante
`src/main/resources/messages.properties`.

## Reglas de catálogo

- El precio debe ser mayor que cero.
- `compareAtPrice`, si existe, debe ser mayor que `price`.
- Solo se pueden consultar productos publicados.
- Para publicar un producto se requiere al menos una imagen y una variante
  activa.
- Las variantes duplicadas por color y talla no están permitidas.
- Colores, tallas y variantes inactivos no se consideran disponibles.
- Las transiciones de estado permitidas son:
  - `DRAFT` → `PUBLISHED` o `ARCHIVED`
  - `PUBLISHED` → `DRAFT` o `ARCHIVED`
  - `ARCHIVED` → `DRAFT`

## Pruebas

Ejecuta la suite disponible con:

```powershell
.\mvnw.cmd test
```

Actualmente existe una prueba de carga del contexto de Spring. Los siguientes
casos deberían incorporarse antes de considerar estable el contrato público:
controller tests, validación de paginación, filtros, ordenamientos, errores
404 y pruebas de integración con PostgreSQL.

## Siguiente nivel para producción

1. Completar autenticación con JWT, usuarios, roles y autorización por recurso.
2. Mover credenciales y configuración por ambiente a variables de entorno o
   un secret manager.
3. Reemplazar `ddl-auto=update` por migraciones versionadas con Flyway o
   Liquibase.
4. Añadir OpenAPI/Swagger real; actualmente las rutas están permitidas, pero el
   proyecto no incluye todavía una dependencia de generación de documentación.
5. Completar CORS explícito, manejo de errores de autenticación y excepciones
   HTTP genéricas.
6. Añadir observabilidad: logs estructurados, correlation ID, métricas,
   health checks y trazas.
7. Añadir CI con compilación, tests, análisis estático y escaneo de
   dependencias.
