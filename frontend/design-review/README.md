# Referencias de Figma y decisiones de Inicio

## Inventario local
Se recorrieron recursivamente las tres carpetas de `/home/isaac/Descargas/Figma/`: 32 archivos PNG. Los originales no se movieron, sobrescribieron ni eliminaron. `inventory.json` registra sus rutas absolutas y dimensiones.

- `DulcePack — Ecommerce Prototype`: 5 archivos; Brand Board, biblioteca local, wireframes, variante extensa de Inicio y composición general.
- `cliente`: 17 archivos; 8 vistas de escritorio, 6 variantes móviles, Header Mobile, Product Tile Mobile y composición general.
- `administracion`: 10 archivos; 7 vistas, barra superior, hotspots de navegación y composición general.

Las hojas de contacto locales (`cliente.jpg`, `administracion.jpg` y `DulcePack — Ecommerce Prototype.jpg`) son derivados de inspección, no recursos de producción; están excluidas de Git.

## Pantallas identificadas y orden propuesto
Solo Inicio está implementada. Cada siguiente vista requiere autorización explícita.

### Cliente
1. Inicio — escritorio y móvil, implementado en `/`.
2. Catálogo — próxima propuesta.
3. Detalle de producto.
4. Carrito.
5. Checkout.
6. Confirmación.
7. Iniciar sesión.
8. Registro.

Los archivos Mobile contienen variantes de las seis primeras vistas, no nuevas rutas.

### Administración, posterior a cliente
1. Login administrativo.
2. Dashboard / Resumen.
3. Productos.
4. Nuevo producto.
5. Inventario.
6. Pedidos.
7. Usuarios.

La navegación lateral morada y el topbar corresponden exclusivamente a administración. Tablas, indicadores, formularios y estados administrativos se identificaron visualmente, pero no se implementaron. Modificación/eliminación de productos y gestión de pedidos/usuarios siguen siendo requisitos futuros.

## Referencia principal de esta entrega
- Escritorio: `/home/isaac/Descargas/Figma/cliente/Cliente / 01 · Inicio.png` (1440 × 1640).
- Móvil: `/home/isaac/Descargas/Figma/cliente/Mobile / 01 · Inicio.png` (390 × 844).
- Componentes y marca: archivos Local Library y Brand Board de la carpeta general.
- La variante general `DulcePack — Inicio.png` (1440 × 2464) contiene otro tratamiento visual: barra promocional, cinco categorías, personalización y otro footer. Se inspeccionó, pero no se mezclaron sus secciones adicionales con la vista cliente para evitar construir un híbrido no solicitado.

## Identidad verificada en Brand Board
- Morado: `#6D28D9`.
- Rosa: `#EC4899`.
- Amarillo: `#FBBF24`.
- Fondo claro: `#FFF9FC`.
- Tinta: `#24123A`.
- Fredoka para títulos y marca; Nunito para texto. Fuentes variables servidas localmente, sin depender de Google Fonts al navegar.
- Escritorio: contenedor de 1296 px, header de 84 px, banner morado con dos columnas, separaciones principales de 24–48 px, categorías en cuatro columnas y cuatro tarjetas. Las medidas se interpretaron a partir del PNG, no de capas Figma.
- Móvil: margen lateral de 16 px, header de 64 px, banner de 244 px, categorías horizontales y productos en dos columnas; botones amarillos y representaciones simples de paquetes como en la exportación.
- Tablet: adaptación propia sin exportación específica, con dos columnas de productos para preservar legibilidad.

## Recursos e interacciones
Solo se recibieron PNG aplanados; no hay archivos de tipografía, SVG ni fotografías independientes. Se extrajeron dos ilustraciones originales como WebP sin pérdida; procedencia y coordenadas en `../src/assets/README.md`. Los iconos de búsqueda y carrito son SVG inline equivalentes, no extracciones vectoriales del PNG.

Interacciones implementadas únicamente dentro de Inicio:
- Buscar entre los cuatro favoritos de demostración, sin distinguir mayúsculas ni acentos.
- Filtrar por categoría, activar/desactivar el filtro y recuperar todos los productos.
- Mostrar estado vacío y limpiar filtros.
- Agregar al contador efímero de demostración, con aviso explícito y reinicio al recargar.
- Catálogo, cuenta y carrito muestran un aviso de próxima vista; no hay pantallas ocultas, autenticación ni transacciones.
- Anclas a Inicio, categorías, favoritos y promoción; búsqueda operable con teclado, Escape y restauración de foco.

Los PNG no conservan hotspots ni navegación funcional del prototipo. Estas interacciones son decisiones de implementación acotadas a Inicio, no interacciones supuestamente verificadas en Figma.

## Diferencias deliberadas frente a las exportaciones
1. El PNG de escritorio muestra bloques blancos vacíos en la zona textual del banner y el footer. Se reemplazaron por texto HTML legible, manteniendo su distribución y el fondo morado; no se pretendió recuperar texto invisible.
2. El móvil superpone el botón sobre el título. Se separaron para no reproducir un problema de lectura.
3. El contador arranca en cero, no en el valor decorativo «2» del diseño, porque no existe un carrito real.
4. En móvil se mantienen Fiesta y Gomitas primero, como la referencia; los otros dos productos, la cuarta categoría y el footer siguen accesibles mediante scroll, en vez de eliminar contenido.
5. El botón rosa usa tinta oscura para contraste. Se añadieron foco visible, enlace de salto, estado vacío y mensajes accesibles.
6. El aviso «Modo demostración» aclara que los precios, beneficios comerciales y productos no representan una tienda operativa. Las categorías asignadas a productos son mock.

## Acceso remoto
Las peticiones a `http://127.0.0.1:9222/json/version` y `http://localhost:9222/json/version` fallaron con curl exit 7 en este entorno. No se utilizó la sesión autenticada de Brave, no se alteró su configuración y no se intentó evadir el bloqueo. La inspección y comparación se hicieron con las exportaciones locales. La herramienta de navegador usada para verificar la aplicación es una sesión separada.
