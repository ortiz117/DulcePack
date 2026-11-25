# Migraciones Flyway

Las migraciones se numeran de forma consecutiva con `V<n>__descripcion.sql` y no se modifican Once aplicadas.

Esquemas incluidos:

- `V1__identity_and_sessions.sql`: usuarios, sesiones, tokens de recuperación e intentos de login.
- `V2__catalog_categories.sql`: categorías de productos (`botanas`, `dulces`, `paquetes`).
- `V3__products_inventory_and_images.sql`: productos, inventario versionado e imágenes.

Las migraciones son administradas exclusivamente por Flyway. Hibernate usa `ddl-auto=validate`, por lo que cualquier cambio de esquema debe realizarse mediante una nueva migración `V<n+1>`.
