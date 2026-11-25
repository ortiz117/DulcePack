# DulcePack Backend

Backend REST de DulcePack. La primera vertical implementada cubre creación, consulta,
administración y reporte resumido de pedidos.

## Módulo de pedidos

Endpoints disponibles:

```text
POST  /api/orders
GET   /api/orders/{code}
GET   /api/admin/orders?page=0&size=20
PATCH /api/admin/orders/{code}/status
GET   /api/admin/reports/orders/summary
```

El cliente envía únicamente `productId`, `quantity` y un `couponCode` opcional por
pedido. Nombre, precio, envío y descuento se resuelven y calculan en el servidor;
cada pedido conserva una instantánea del producto. Los códigos públicos utilizan
el formato `DP-000001` y admiten seis o más dígitos.

Flujo de estados permitido:

```text
PENDING -> PREPARING -> SHIPPED -> DELIVERED
PENDING -> CANCELLED
PREPARING -> CANCELLED
```

Las rutas administrativas requieren el rol `ADMIN`. El detalle de un pedido solo
puede consultarlo su propietario autenticado o un administrador. La integración
definitiva de identidad puede reemplazar la autenticación temporal sin acoplar el
módulo de pedidos al catálogo ni a usuarios concretos.

## Requisitos

- Java 21
- PostgreSQL para el perfil `dev`
- Maven Wrapper incluido

## Perfiles

### `dev`

Usa PostgreSQL y Flyway. Configura las credenciales mediante variables de entorno:

```text
SPRING_PROFILES_ACTIVE=dev
DB_URL=jdbc:postgresql://localhost:5432/dulcepack
DB_USERNAME=dulcepack
DB_PASSWORD=<definida localmente>
```

No se incluye ninguna contraseña en el repositorio.

### `test`

Usa una base H2 en memoria y no requiere PostgreSQL. Ejecuta las mismas migraciones
Flyway que el perfil de desarrollo y valida el esquema con Hibernate.

## Verificación

En Windows PowerShell:

```powershell
.\mvnw.cmd clean verify
```

En Bash:

```bash
./mvnw clean verify
```

Las migraciones futuras deben almacenarse en `src/main/resources/db/migration` y ser administradas exclusivamente por Flyway.
