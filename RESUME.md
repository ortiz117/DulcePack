# DulcePack — seguimiento y reanudación

## Estado actual

El prototipo frontend está funcionalmente completo respecto a las pantallas identificadas en las exportaciones locales de Figma.

Todo el trabajo posterior al commit remoto `0da3fbc` permanece local hasta que el usuario autorice explícitamente subirlo. No ejecutar `git push`, publicar ramas ni actualizar el remoto sin esa autorización.

## Ubicaciones

```text
Repositorio: C:\Users\julia\Desktop\DulcePack
Frontend:   C:\Users\julia\Desktop\DulcePack\frontend
Diseños:    C:\Users\julia\Desktop\Figma
```

## Tecnologías

- Svelte 5 con runas.
- TypeScript.
- Vite.
- Playwright con proyectos desktop y mobile.
- axe-core/playwright para comprobaciones automáticas WCAG A/AA.
- Fredoka y Nunito mediante Fontsource.
- Backend previsto: Java con Spring Boot.
- Base de datos prevista: PostgreSQL.

## Arquitectura

```text
DulcePack/
├── frontend/
│   ├── src/
│   │   ├── assets/          # Ilustraciones y fuentes
│   │   ├── components/      # Shells, encabezado, footer y UI compartida
│   │   ├── data/            # Productos y datos administrativos mock
│   │   ├── pages/           # Vistas cliente y administración
│   │   ├── styles/          # Tokens y estilos globales
│   │   ├── types/
│   │   ├── App.svelte       # Estado global efímero y enrutamiento cliente
│   │   └── main.ts
│   ├── tests/               # Playwright, responsive y accesibilidad
│   ├── design-review/       # Inventario de exportaciones y evidencia
│   ├── package.json
│   └── playwright.config.ts
├── backend/                 # Vacío y reservado
├── README.md
└── RESUME.md
```

No se utiliza una biblioteca de router. `App.svelte` resuelve rutas con History API, normaliza rutas administrativas malformadas y protege `/admin/*` mediante una sesión local de demostración.

## Vistas implementadas

### Cliente

- `/` — Inicio.
- `/catalogo` — Catálogo, búsqueda y filtros.
- `/producto/:id` — Detalle de producto.
- `/carrito` — Carrito y cantidades.
- `/checkout` — Checkout demostrativo.
- `/confirmacion` — Confirmación demostrativa.
- `/iniciar-sesion` — Acceso de cliente mock.
- `/registro` — Registro mock.

### Administración

- `/admin` — Login administrativo mock.
- `/admin/dashboard` — Resumen, métricas y acciones rápidas.
- `/admin/productos` — Productos y búsqueda.
- `/admin/productos/nuevo` — Alta demostrativa.
- `/admin/productos/:id/editar` — Edición demostrativa.
- `/admin/inventario` — Métricas y tabla de existencias.
- `/admin/pedidos` — Listado de pedidos.
- `/admin/usuarios` — Listado de usuarios.

Todas las vistas exportadas de cliente y administración identificadas en `frontend/design-review/inventory.json` tienen una implementación correspondiente.

## Comportamiento implementado

- Navegación SPA con historial, Back/Forward y normalización de rutas administrativas inválidas.
- Rutas administrativas protegidas por `sessionStorage` en modo demostración.
- Catálogo, detalle, carrito, checkout y confirmación conectados.
- Registro e inicio de sesión de cliente mock.
- Dashboard y navegación administrativa completa.
- Alta y edición local de productos con validación nativa.
- Selección y drag-and-drop de imágenes `image/*`, sin subida real.
- Inventario, pedidos y usuarios con datos tipados mock.
- Layouts responsive y tablas desplazables accesibles en móvil.
- Estados, errores y confirmaciones anunciados con semántica accesible.

## Datos y seguridad

Todo es simulado:

- No hay API, backend ni PostgreSQL configurado.
- No hay autenticación o autorización de servidor.
- Las credenciales administrativas están visibles en el frontend.
- El carrito, sesiones y formularios no persisten en una base de datos.
- No hay pagos, pedidos, correos ni archivos subidos realmente.

Credenciales administrativas de demostración:

```text
Correo:     admin@dulcepack.mx
Contraseña: DulcePack2026
```

No reutilizar este modelo de autenticación en producción.

## Ejecución

```bash
cd C:/Users/julia/Desktop/DulcePack/frontend
npm install
npm run dev -- --port 5173 --strictPort
```

Servidor local:

```text
http://127.0.0.1:5173/
```

Pruebas:

```bash
npm run check
npm run build
npm test
```

Playwright inicia o reutiliza Vite en `127.0.0.1:5174` para las pruebas.

## Verificación acumulada

- Desarrollo realizado con ciclos RED → GREEN para las nuevas funciones.
- `svelte-check`: sin errores ni advertencias en las verificaciones finales de cada módulo.
- Build de Vite: correcto.
- Suite Playwright: escritorio y móvil.
- Cobertura de anchos: 320, 390, 768, 1024 y 1440 px; Inicio también se probó hasta 1920 px.
- Comprobaciones axe WCAG A/AA en las vistas principales y administrativas.
- Rutas inválidas de edición, historial, sesión administrativa y estados transitorios cubiertos por regresiones.
- Los cambios funcionales pasaron revisiones independientes antes de sus commits locales.

Ejecutar nuevamente la suite completa antes de integrar o subir; el resultado final de esa ejecución debe registrarse en el informe de entrega, no suponerse a partir de resultados anteriores.

## Estado Git local

Los commits locales posteriores al estado remoto incluyen:

- `b87f51f` — formulario de nuevo producto.
- `e0a5187` — inventario administrativo.
- `8e744ca` — edición de productos y navegación administrativa completa.

La documentación y cualquier ajuste final posterior pueden generar commits locales adicionales. El remoto debe permanecer sin cambios hasta autorización del usuario.

## Límites conocidos

- Sin backend, persistencia, pagos ni seguridad de producción.
- Datos y métricas mock.
- El detalle de pedido solo entrega feedback demostrativo; no existe vista adicional en Figma.
- Las comprobaciones de accesibilidad automatizadas no sustituyen pruebas manuales con lector de pantalla.
- Verificado principalmente en Chromium automatizado; faltan Firefox, WebKit y dispositivos físicos.

## Verificación final local

Ejecutada sobre `develop` local antes de la entrega:

- `npm run check`: 0 errores y 0 advertencias.
- `npm run build`: correcto; 158 módulos transformados.
- `npm test`: 110 pruebas aprobadas, 4 omitidas intencionalmente y 0 fallidas.
- `git status`: árbol de trabajo limpio después del commit documental final.
- No se ejecutó `git push`; al confirmar este ajuste documental, `develop` local queda cinco commits por delante de `origin/develop`.

## Próxima fase técnica

Si el usuario decide continuar más allá del prototipo frontend:

1. Diseñar contratos de API y modelo de datos.
2. Crear Spring Boot y PostgreSQL.
3. Implementar autenticación y autorización reales.
4. Reemplazar datos mock por servicios HTTP.
5. Persistir carrito, productos, inventario, usuarios y pedidos.
6. Integrar pagos únicamente con un proveedor y requisitos de seguridad definidos.

No iniciar esa fase ni subir los cambios locales actuales sin autorización explícita.
