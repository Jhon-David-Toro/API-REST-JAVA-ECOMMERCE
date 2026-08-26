# Ecommerce API

API REST para una plataforma de comercio electrónico, construida con Java 21,
Spring Boot, Spring Web MVC, Spring Data JPA, Spring Security y PostgreSQL.

> **Estado actual:** el catálogo público de productos es el módulo funcional
> expuesto por HTTP. Los módulos de autenticación, usuarios, carrito, pedidos,
> marcas y categorías tienen clases iniciales, pero todavía no exponen
> operaciones de negocio completas.

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

### Endpoint técnico actual

```http
GET /users
```

Es una ruta protegida por la configuración global y actualmente responde
`API funcionando`. No representa todavía un recurso de usuarios completo.

## Seguridad

La configuración actual está en
`src/main/java/com/david/ecommerce_api/config/SecurityConfig.java`:

- `/api/public/**` permite acceso sin autenticación.
- `/swagger-ui/**` y `/v3/api-docs/**` están permitidos para documentación.
- Cualquier otra ruta exige autenticación.
- Se deshabilita CSRF porque la API está planteada como backend REST.
- El mecanismo configurado actualmente es HTTP Basic.

La aplicación todavía no define un flujo de login ni JWT. Por ello, HTTP Basic
debe considerarse una configuración temporal/de desarrollo y usarse únicamente
con HTTPS y credenciales gestionadas de forma segura.

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
