# Informe de verificación — Inicio

Objetivo: `http://127.0.0.1:5173/`.
Alcance: exclusivamente la vista de cliente Inicio, escritorio, móvil y adaptación tablet.

## Resumen
- `npm install`: correcto, incluida la repetición solicitada tras comprobar Node y npm.
- `npm run check`: 0 errores y 0 advertencias.
- `npm run build`: compilación de producción correcta.
- `npm test`: 19 pruebas exitosas, 1 omitida intencionalmente. La omitida prueba el enlace «Ver todo» de escritorio, que no existe en móvil; el restablecimiento móvil se verifica mediante «Mostrar todos los favoritos».
- Las pruebas funcionales se escribieron y ejecutaron en rojo antes de implementar cada comportamiento, luego se verificaron en verde.
- Axe: sin infracciones detectadas en las comprobaciones automatizadas WCAG A/AA seleccionadas para Inicio y su estado de búsqueda vacía. No constituye una certificación completa de accesibilidad.
- Sin errores de consola o recursos HTTP fallidos en el alcance probado.
- Layout sin desbordamiento horizontal de página a 320, 390, 768, 1024, 1440 y 1920 px. La lista móvil de categorías tiene scroll horizontal intencional.
- Revisión visual manual de capturas completas de escritorio, tablet y móvil contra las exportaciones; diferencias deliberadas en `README.md` de esta carpeta.
- README raíz y LICENSE sin modificaciones; backend vacío y sin cambios.

## Incidencia técnica pendiente
Una causa raíz de severidad alta produce 6 alertas en `npm audit`: ReDoS de `picomatch` (GHSA-fc5q-39xc-vjpf) y sus dependientes de desarrollo (`fdir`, `tinyglobby`, `vite`, `svelte-check`, `@sveltejs/vite-plugin-svelte`).

Reproducción:

```bash
cd /home/isaac/Escritorio/DulcePack/frontend/
npm audit
npm audit --omit=dev
```

Resultado: auditoría completa con 6 alertas altas; auditoría de dependencias de producción con 0 vulnerabilidades. El registro consultado devolvió `picomatch` 4.0.7 como versión más reciente; el rango afectado indicado incluye esa versión. npm propone degradaciones mayores de herramientas, no una actualización compatible. No se aplicó `npm audit fix --force`, no se ocultó la alerta y no se considera resuelta.

Recomendación: revisar una actualización corregida y compatible antes de publicar. El servidor de revisión escucha solamente en 127.0.0.1; no se publicó ni expuso a la red.

## Correcciones verificadas durante la revisión
- «Ver todo» ahora elimina filtros además de desplazar a favoritos; prueba de regresión ejecutada.
- Concordancia singular del contador accesible al agregar un producto.
- Selector de CSS corregido para que svelte-check no produzca advertencias.
- La comprobación de imágenes distingue imágenes lazy ocultas en móvil de recursos rotos: verifica carga de las visibles y respuesta HTTP de cada recurso único. No se forzó la descarga innecesaria de imágenes ocultas.

## Evidencia visual
Capturas generadas desde un navegador real y revisadas:
- `screenshots/desktop.png` — viewport 1440 × 1000, página completa.
- `screenshots/tablet.png` — viewport 768 × 1024, página completa.
- `screenshots/mobile.png` — viewport 390 × 844, página completa.

Rutas absolutas bajo `/home/isaac/Escritorio/DulcePack/frontend/design-review/`. Son evidencia local excluida de Git, no imágenes utilizadas para renderizar la aplicación.

## Cobertura y límites
Verificado: renderizado, catálogo mock de favoritos, filtros, búsqueda con acentos, estado vacío, recuperación, navegación interna, avisos de vistas pendientes, contador simulado y reinicio, cierre de avisos, teclado, foco, consola, recursos y responsive.

Fuera de alcance: segunda vista, backend, servicios de base de datos, compras, cuentas, inventario real, pagos, persistencia del carrito, Firefox/Safari y pruebas en teléfonos físicos. No se afirmó inspección autenticada de Figma ni fidelidad pixel-perfect de textos que no son visibles en los PNG.

Estado: PAUSADO — Esperando supervisión.
