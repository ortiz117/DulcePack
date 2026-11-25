# DulcePack Backend

Backend REST de DulcePack con Java 21, Spring Boot, PostgreSQL, Spring Security y Flyway.

## Requisitos

- Java 21
- PostgreSQL
- Maven Wrapper incluido
- Docker opcional para las pruebas Testcontainers

## Ejecución

```bash
export SPRING_PROFILES_ACTIVE=dev
export DB_URL=jdbc:postgresql://localhost:5432/dulcepack
export DB_USERNAME=dulcepack
export DB_PASSWORD='cambiar-en-local'
export SECURE_COOKIES=false # solo desarrollo HTTP local

bash ./mvnw spring-boot:run
```

Para crear el primer administrador, agrega temporalmente al primer arranque:

```bash
export ADMIN_EMAIL=admin@example.com
export ADMIN_PASSWORD='Adm1n!SecurePassword'
export ADMIN_DISPLAY_NAME='Administrador'
```

La contraseña se almacena con BCrypt y las variables pueden retirarse después de crear la cuenta.

Los límites de login, registro y recuperación se configuran con las variables
`MAX_LOGIN_FAILURES`, `MAX_ORIGIN_LOGIN_FAILURES`, `MAX_REGISTRATION_REQUESTS`,
`MAX_PASSWORD_RESET_REQUESTS` y `MAX_ORIGIN_PASSWORD_RESET_REQUESTS`. El encabezado
`X-Forwarded-For` solo se utiliza cuando `TRUST_FORWARDED_FOR=true`; actívalo únicamente
detrás de un proxy confiable que elimine encabezados enviados directamente por clientes.

## Recuperación de contraseña por correo

```bash
export MAIL_ENABLED=true
export MAIL_HOST=smtp.example.com
export MAIL_PORT=587
export MAIL_USERNAME=usuario
export MAIL_PASSWORD='secreto-local'
export MAIL_SMTP_AUTH=true
export MAIL_STARTTLS=true
export MAIL_FROM=no-reply@example.com
export PASSWORD_RESET_BASE_URL=https://app.example.com/reset-password
```

## Endpoints de identidad

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `GET /api/users/me`
- `GET /api/admin/users`
- `PATCH /api/admin/users/{id}/status`

Las sesiones usan cookies HttpOnly. Los endpoints autenticados que modifican estado requieren el token de la cookie `XSRF-TOKEN` en el encabezado `X-XSRF-TOKEN`.

## Endpoints de catálogo e inventario

Públicos:

- `GET /api/catalog/categories` — devuelve `id`, `slug`, `name` y `shortName`.
- `GET /api/catalog/products` — filtros opcionales `query`, `category`, `flavor`, `minPrice`, `maxPrice`; devuelve como máximo 100 resultados.
- `GET /api/catalog/products/page` — misma búsqueda con `page` y `size` (máximo 100).
- `GET /api/catalog/products/{slug}`
- `GET /api/catalog/products/{slug}/image` — imagen raster con `nosniff` y CSP `sandbox`.

Administrativos (requieren rol `ADMIN`):

- `GET /api/admin/products` — devuelve como máximo 100 resultados; usa `GET /api/catalog/products/page` para recorrer el catálogo completo.
- `GET /api/admin/products/{slug}`
- `POST /api/admin/products` — `multipart/form-data` con parte JSON `product` y parte opcional `image`.
- `PUT /api/admin/products/{slug}` — mismo formato.
- `DELETE /api/admin/products/{slug}`

El campo `minimumStock` de `product` es opcional y permite publicar umbrales de stock bajo. Las imágenes aceptadas son PNG, JPEG y GIF; se validan por firma de bytes y por decodificación estructural completa (el formato declarado debe coincidir con el contenido decodificado, con un máximo de 20 megapíxeles y 5 MB). El slug se deriva del nombre en la creación y permanece estable en las ediciones.

## Endpoints de pedidos y reportes

Clientes autenticados:

- `POST /api/orders` — crea un pedido con `productId`, `quantity` y cupón opcional; importes y envío se calculan en el servidor.
- `GET /api/orders/{code}` — consulta un pedido propio mediante códigos `DP-000001` de seis o más dígitos.

Administrativos (requieren rol `ADMIN`):

- `GET /api/admin/orders?page=0&size=20`
- `PATCH /api/admin/orders/{code}/status`
- `GET /api/admin/reports/orders/summary`

Cada pedido conserva una instantánea del nombre y precio de sus productos. El flujo permitido es `PENDING -> PREPARING -> SHIPPED -> DELIVERED`; los pedidos `PENDING` o `PREPARING` también pueden cancelarse.

## CORS

Los orígenes permitidos se configuran con `CORS_ALLOWED_ORIGINS` (por defecto `http://localhost:5173,http://127.0.0.1:5173`). El frontend consume la API desde `src/lib/api/catalog.ts`.

## Pruebas y compilación

```bash
bash ./mvnw clean verify
```

Las pruebas normales usan H2 en modo PostgreSQL y ejecutan Flyway. Si Docker está disponible, Testcontainers valida además la migración contra PostgreSQL real; sin Docker esa prueba se omite.

Las migraciones se almacenan en `src/main/resources/db/migration` y son administradas exclusivamente por Flyway. Hibernate usa `ddl-auto=validate`.
