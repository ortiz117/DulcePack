# DulcePack Backend

Base técnica del backend REST de DulcePack. Todavía no contiene entidades, controladores ni lógica de negocio.

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

Usa una base H2 en memoria y no requiere PostgreSQL. Flyway permanece desactivado hasta que existan migraciones compatibles con PostgreSQL.

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
