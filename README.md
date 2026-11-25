# DulcePack 🍬

DulcePack es un proyecto académico de comercio electrónico para paquetes de dulces y snacks. Incluye una experiencia de compra responsive para clientes y un panel administrativo de demostración.

## Estado del proyecto

El frontend del prototipo está implementado con todas las pantallas identificadas en los diseños de Figma.

El frontend conserva datos mock para el carrito, pedidos y métricas, mientras que el backend ya expone autenticación segura, usuarios, catálogo e inventario con PostgreSQL y Flyway. Los pagos y la persistencia de pedidos todavía no están implementados.

## Funcionalidades implementadas

### Cliente

- Inicio, promociones, categorías y productos destacados.
- Catálogo con búsqueda y filtros.
- Detalle de producto y selección de cantidad.
- Carrito con actualización y eliminación de productos.
- Checkout y confirmación de pedido demostrativo.
- Registro e inicio de sesión demostrativos.
- Diseño responsive para escritorio, tablet y móvil.

### Administración

- Acceso administrativo demostrativo.
- Dashboard con métricas, gráfica y accesos rápidos.
- Listado y búsqueda de productos.
- Creación y edición de productos en modo demostración.
- Selección y arrastre de imágenes con validación local.
- Inventario con métricas y estados de existencias.
- Listado de pedidos y consulta demostrativa de detalles.
- Listado de usuarios.
- Navegación protegida mediante una sesión local de demostración.

## Tecnologías

| Capa | Tecnología |
| --- | --- |
| Frontend | Svelte 5, TypeScript y Vite |
| Pruebas | Playwright y axe-core |
| Fuentes | Fredoka y Nunito mediante Fontsource |
| Backend futuro | Java con Spring Boot |
| Base de datos futura | PostgreSQL |
| Diseño | Figma |

## Estructura

```text
DulcePack/
├── frontend/
│   ├── src/
│   │   ├── assets/
│   │   ├── components/
│   │   ├── data/
│   │   ├── pages/
│   │   ├── styles/
│   │   ├── types/
│   │   ├── App.svelte
│   │   └── main.ts
│   ├── tests/
│   ├── design-review/
│   ├── package.json
│   └── playwright.config.ts
├── backend/                 # API Spring Boot: identidad, catálogo e inventario
├── README.md
└── RESUME.md
```

## Ejecución local

Requisitos: Node.js y npm.

```bash
cd frontend
npm install
npm run dev -- --port 5173 --strictPort
```

Abrir:

```text
http://127.0.0.1:5173/
```

## Acceso administrativo de demostración

```text
URL:        http://127.0.0.1:5173/admin
Correo:     admin@dulcepack.mx
Contraseña: DulcePack2026
```

Estas credenciales y la sesión guardada en `sessionStorage` son exclusivamente demostrativas. No constituyen autenticación apta para producción.

## Rutas principales

### Cliente

```text
/
/catalogo
/producto/:id
/carrito
/checkout
/confirmacion
/iniciar-sesion
/registro
```

### Administración

```text
/admin
/admin/dashboard
/admin/productos
/admin/productos/nuevo
/admin/productos/:id/editar
/admin/inventario
/admin/pedidos
/admin/usuarios
```

## Verificación

Desde `frontend/`:

```bash
npm run check
npm run build
npm test
```

Playwright prueba proyectos de escritorio y móvil. Las suites incluyen navegación, validaciones, rutas protegidas, comportamiento responsive, ausencia de overflow horizontal y comprobaciones automáticas WCAG A/AA con axe-core.

Si Chromium no está instalado para Playwright:

```bash
npx playwright install chromium
```

## Limitaciones

- Todos los productos, usuarios, pedidos, métricas y credenciales son mock.
- El carrito y las sesiones son efímeros y se reinician al recargar o cerrar la sesión correspondiente.
- El panel admin todavía usa datos mock; la API ya persiste productos, inventario e imágenes en PostgreSQL.
- El checkout no procesa pagos ni crea pedidos reales.
- El backend implementa identidad, catálogo e inventario; pedidos, carrito y pagos siguen siendo mock.
- La aplicación se ha verificado principalmente con Chromium automatizado; falta validación manual en otros navegadores y dispositivos físicos.

## Próxima etapa

La siguiente fase técnica es conectar el frontend a los endpoints del backend mediante `src/lib/api/catalog.ts`, y posteriormente implementar carrito, pedidos y pagos reales.
