# Migraciones Flyway

Las migraciones se numeran de forma consecutiva con `V<n>__descripcion.sql` y no se modifican una vez aplicadas.

Esquemas incluidos:

- `V1__identity_and_sessions.sql`: usuarios, sesiones, tokens de recuperación e intentos de login.
- `V2__catalog_categories.sql`: categorías de productos (`botanas`, `dulces`, `paquetes`).
- `V3__products_inventory_and_images.sql`: productos, inventario versionado e imágenes.
- `V4__cart_and_promotions.sql`: carritos, artículos de carrito y cupones (`DULCE50`).
- `V5__create_orders.sql`: pedidos, artículos de pedido y estados de entrega.

Las migraciones son administradas exclusivamente por Flyway. Hibernate usa `ddl-auto=validate`, por lo que cualquier cambio de esquema debe realizarse mediante una nueva migración `V<n+1>`.
