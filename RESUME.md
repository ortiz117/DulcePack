# DulcePack — seguimiento y reanudación

## Estado actual
PAUSADO — Esperando supervisión.

Primera vista implementada: **Cliente / Inicio**, con variantes responsive de escritorio, tablet y móvil, exclusivamente en `/`. No se implementó ninguna segunda vista. No continuar sin autorización explícita del usuario.

## Ubicaciones y preservación
- Monorepo: `/home/isaac/Escritorio/DulcePack/`.
- Frontend: `/home/isaac/Escritorio/DulcePack/frontend/`.
- Diseños originales: `/home/isaac/Descargas/Figma/`; inspección recursiva de 32 PNG en las tres carpetas, sin moverlos ni modificarlos.
- README raíz y LICENSE preservados sin cambios, comprobados con Git.
- `backend/` permanece vacío y no se modificó en esta ejecución.
- Sin commits, push ni publicaciones.

## Tecnologías definitivas
- Frontend instalado: Svelte 5.58.0, TypeScript 5.9.3 y Vite 8.3.8.
- Plugin de Svelte 7.3.1 y svelte-check 4.7.8.
- Fuentes locales Fredoka y Nunito mediante Fontsource 5.3.0.
- Pruebas: Playwright 1.65.1 y axe-core/playwright 4.14.0.
- Entorno comprobado: Node.js v26.12.1, npm 12.0.2. Están disponibles en el entorno de Hermes; no fue necesario cargar NVM. `npm install` se ejecutó correctamente y se repitió tras la indicación del usuario.
- Backend FUTURO: Java, Spring Boot y **PostgreSQL**. PostgreSQL sustituye definitivamente a MySQL por instrucción explícita del usuario. No implementar todavía backend, endpoints ni configuraciones de base de datos.
- `package-lock.json` conserva las versiones resueltas.

## Arquitectura actual

```text
DulcePack/
├── frontend/
│   ├── src/
│   │   ├── assets/             # Ilustraciones extraídas y procedencia
│   │   ├── components/         # Header, Footer, Button, ProductCard, Icon
│   │   ├── data/home.ts        # Productos/categorías mock separados
│   │   ├── pages/Home.svelte   # Única vista implementada
│   │   ├── styles/global.css  # Tokens, fuentes, foco y reglas globales
│   │   ├── types/product.ts
│   │   ├── App.svelte
│   │   └── main.ts
│   ├── tests/                 # Comportamiento y calidad
│   ├── design-review/         # Inventario, análisis y evidencia local
│   ├── index.html
│   ├── package.json
│   ├── package-lock.json
│   ├── vite.config.ts
│   ├── svelte.config.js
│   ├── tsconfig.json
│   └── playwright.config.ts
├── backend/                   # Vacío, reservado
├── README.md                  # Original, intacto
├── LICENSE                    # Original, intacto
├── RESUME.md
└── .gitignore
```

No hay router, stores globales, capa de servicios ni directorios vacíos innecesarios para una sola vista. El estado de Inicio es local con runas de Svelte. Los tipos y datos mock están separados para poder incorporar una API más adelante; no se inventó un contrato de backend.

## Referencias y decisiones visuales
Referencias principales:
- `/home/isaac/Descargas/Figma/cliente/Cliente / 01 · Inicio.png`.
- `/home/isaac/Descargas/Figma/cliente/Mobile / 01 · Inicio.png`.
- Brand Board y Local Library de `/home/isaac/Descargas/Figma/DulcePack — Ecommerce Prototype/`.

Se utilizaron los colores exactos indicados en Brand Board: morado #6D28D9, rosa #EC4899, amarillo #FBBF24, claro #FFF9FC y tinta #24123A. Se extrajeron dos ilustraciones originales sin pérdida; coordenadas y procedencia en `frontend/src/assets/README.md`.

Las exportaciones muestran bloques vacíos en el banner/footer de escritorio y solapamiento de título/botón en móvil. Se proporcionó texto legible sin reproducir esos defectos. Se mantuvieron todos los productos accesibles en móvil, y se inició el contador en cero en vez del «2» decorativo. Estas diferencias requieren supervisión visual; no se afirma fidelidad pixel-perfect.

La otra variante general de Inicio se inspeccionó, pero no se mezclaron sus secciones adicionales con Cliente Inicio. Inventario, vistas identificadas y decisiones completas: `frontend/design-review/README.md` y `inventory.json`.

## Acceso a Figma y navegador
- Los intentos de consultar CDP en `127.0.0.1:9222` y `localhost:9222` devolvieron curl exit 7. No se pudo utilizar la sesión autenticada de Brave desde este entorno.
- Se utilizó la alternativa autorizada: las tres carpetas locales.
- Los PNG no contienen capas editables ni comportamiento interactivo. No atribuir al prototipo interacciones que solo se dedujeron o implementaron aquí.
- Se verificó la aplicación local mediante la herramienta de navegador y Chromium/Playwright, en una sesión independiente.

## Qué funciona y qué es simulado
Real, dentro de esta vista:
- Renderizado responsive del encabezado, banner, categorías, favoritos, beneficios y footer.
- Búsqueda local sobre los cuatro favoritos, tolerante a mayúsculas/acentos.
- Filtros de categoría, estado vacío y restablecimiento de favoritos.
- Navegación por anclas, teclado, foco visible, cierre de búsqueda con Escape y mensajes accesibles.

Simulado:
- Productos, precios MXN, categorías, ilustraciones comerciales y beneficios previstos.
- Agregar solo incrementa un contador efímero; se reinicia al recargar. No existe un carrito persistente, inventario, compra, pedido ni cobro.
- Catálogo, Mi cuenta y Carrito muestran un aviso de «próxima vista», sin abrir pantallas nuevas.
- Footer y avisos identifican expresamente el modo de demostración.

## Vistas pendientes, una por autorización
Cliente:
1. Catálogo — siguiente propuesta.
2. Detalle de producto.
3. Carrito.
4. Checkout.
5. Confirmación.
6. Iniciar sesión.
7. Registro.

Administración, posteriormente:
1. Login administrativo.
2. Dashboard/Resumen.
3. Productos.
4. Nuevo producto.
5. Inventario.
6. Pedidos.
7. Usuarios.

Las seis variantes móviles exportadas de cliente corresponden a las seis primeras pantallas de ese flujo, contando Inicio; no son pantallas adicionales para implementar sin autorización.

## Comandos de ejecución Linux
El servidor de revisión se inició y devolvió esta URL real:

**http://127.0.0.1:5173/**

Ruta de la única vista: `/`.

Si el servidor sigue activo, abrir la URL directamente; no arrancar una segunda instancia sobre el mismo puerto. Para iniciarlo de nuevo cuando esté detenido:

```bash
cd /home/isaac/Escritorio/DulcePack/frontend/
npm install
npm run dev -- --port 5173 --strictPort
```

Las dependencias ya están instaladas. `npm install` es necesario al configurar por primera vez o cambiar dependencias; puede omitirse para una revisión normal. `--strictPort` evita que Vite cambie silenciosamente de puerto. El servidor escucha solo en 127.0.0.1 y no se expuso a la red.

Si una terminal nueva no encuentra Node/npm y el usuario utiliza NVM:

```bash
[ -s "$HOME/.nvm/nvm.sh" ] && . "$HOME/.nvm/nvm.sh"
node --version
npm --version
```

No reinstalar Node ni modificar el sistema sin diagnosticar primero un error concreto.

Comprobaciones:

```bash
cd /home/isaac/Escritorio/DulcePack/frontend/
npm run check
npm run build
npm test
npm audit
npm audit --omit=dev
```

En otra máquina, instalar primero el navegador de pruebas con `npx playwright install chromium`. Playwright inicia su propio servidor de pruebas en `127.0.0.1:5174`, diferente del servidor de revisión.

## Verificaciones ejecutadas
- `npm run check`: 0 errores y 0 advertencias.
- `npm run build`: correcto, salida en `frontend/dist/`.
- `npm test`: 19 pruebas exitosas y 1 omitida intencionalmente, sin fallos. La omitida es el enlace «Ver todo» exclusivo de escritorio en el proyecto móvil; el restablecimiento móvil sí está probado.
- TDD: se observaron fallos por funcionalidad ausente antes de implementar los comportamientos; suite final en verde.
- Pruebas responsive de ancho: 320, 390, 768, 1024, 1440 y 1920 px, sin overflow horizontal de página.
- Axe: sin infracciones detectadas en las reglas automatizadas WCAG A/AA seleccionadas. No sustituye una auditoría manual completa.
- Consola, imágenes y enlaces internos comprobados; sin errores en el alcance probado.
- Revisión visual de escritorio, tablet y móvil contra los PNG originales.
- `git diff --check`: correcto; README y LICENSE sin diferencias; backend comprobado vacío.

Evidencia:
- `/home/isaac/Escritorio/DulcePack/frontend/design-review/screenshots/desktop.png`.
- `/home/isaac/Escritorio/DulcePack/frontend/design-review/screenshots/tablet.png`.
- `/home/isaac/Escritorio/DulcePack/frontend/design-review/screenshots/mobile.png`.
- Informe: `/home/isaac/Escritorio/DulcePack/frontend/design-review/report.md`.

## Problemas conocidos y límites
- `npm audit` informa 6 alertas altas en herramientas de desarrollo, derivadas de una vulnerabilidad ReDoS de `picomatch` (GHSA-fc5q-39xc-vjpf). La versión más reciente consultada, 4.0.7, figura dentro del rango afectado. npm sugiere degradaciones mayores de herramientas. No se aplicó `--force`; revisar una corrección compatible antes de publicar.
- `npm audit --omit=dev`: 0 vulnerabilidades en las dependencias de producción. Esto no implica una garantía total de seguridad.
- Solo se probó Chromium, no Safari/Firefox ni dispositivos físicos.
- No se verificaron interacciones autenticadas de Figma; los originales locales son imágenes aplanadas.
- Las diferencias visuales deliberadas se documentan para la supervisión del usuario.

## Último punto de pausa y próxima tarea
Inicio está entregado para supervisión. Esperar comentarios o autorización expresa. La próxima tarea propuesta es **Cliente / 02 · Catálogo**, contrastando su exportación de escritorio y móvil, reutilizando los componentes existentes y sin desarrollar otra vista simultáneamente. Ante correcciones de Inicio, limitarse a esas correcciones.

**PAUSADO — Esperando supervisión.**
