import { test, expect } from '@playwright/test';

test('Ver todo elimina el filtro de categoría en escritorio', async ({ page, isMobile }) => {
  test.skip(isMobile, 'El diseño móvil usa el botón Mostrar todos los favoritos.');
  await page.goto('/');
  await page.locator('#categorias').getByRole('button', { name: /Gomitas/ }).click();
  await expect(page.getByRole('article')).toHaveCount(1);
  await page.getByRole('link', { name: 'Ver todo', exact: false }).click();
  await expect(page.getByRole('article')).toHaveCount(4);
});

test('la búsqueda local admite acentos y muestra un estado vacío recuperable', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Buscar antojos', exact: true }).click();
  const search = page.getByRole('searchbox', { name: 'Buscar entre favoritos' });
  await expect(search).toBeFocused();
  await search.fill(' ACIDAS ');
  await expect(page.getByRole('article')).toHaveCount(1);
  await expect(page.getByRole('heading', { name: 'Mix Gomitas', exact: true })).toBeVisible();
  await search.fill('producto-inexistente');
  await expect(page.getByRole('article')).toHaveCount(0);
  await expect(page.getByText('No encontramos favoritos con esos filtros.')).toBeVisible();
  await page.getByRole('button', { name: 'Mostrar todos los favoritos' }).click();
  await expect(page.getByRole('article')).toHaveCount(4);
  await expect(search).toHaveValue('');
  await search.press('Escape');
  await expect(search).not.toBeVisible();
  await expect(page.getByRole('button', { name: 'Buscar antojos', exact: true })).toBeFocused();
});

test('las categorías filtran los favoritos sin abrir una segunda vista', async ({ page }) => {
  await page.goto('/');
  await page.locator('#categorias').getByRole('button', { name: /Gomitas/ }).click();
  await expect(page.getByRole('article')).toHaveCount(1);
  await expect(page.getByRole('heading', { name: 'Mix Gomitas', exact: true })).toBeVisible();
  await expect(page.locator('#categorias').getByRole('button', { name: /Gomitas/ })).toHaveAttribute('aria-pressed', 'true');
  await page.getByRole('button', { name: 'Mostrar todos los favoritos' }).click();
  await expect(page.getByRole('article')).toHaveCount(4);
  expect(new URL(page.url()).pathname).toBe('/');
});

test('las vistas pendientes informan sin navegar y permiten cerrar el aviso', async ({ page, isMobile }) => {
  await page.goto('/');
  await page.getByRole('button', { name: isMobile ? 'Ver catálogo' : 'Explorar catálogo', exact: true }).click();
  await expect(page.getByRole('status')).toContainText('Catálogo: disponible en una próxima vista');
  await page.getByRole('button', { name: 'Cerrar aviso' }).click();
  await expect(page.getByRole('status')).toBeEmpty();
  await page.getByRole('button', { name: 'Carrito de demostración: 0 productos', exact: true }).click();
  await expect(page.getByRole('status')).toContainText('Carrito: disponible en una próxima vista');
  await page.getByRole('button', { name: 'Cerrar aviso' }).click();
  if (!isMobile) {
    await page.getByRole('button', { name: 'Mi cuenta', exact: true }).click();
    await expect(page.getByRole('status')).toContainText('Mi cuenta: disponible en una próxima vista');
  }
  expect(new URL(page.url()).pathname).toBe('/');
});

test('agregar actualiza solamente el contador de demostración y se reinicia al recargar', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('button', { name: 'Carrito de demostración: 0 productos', exact: true })).toBeVisible();
  const add = page.getByRole('button', { name: 'Agregar Pack Fiesta Dulce al carrito de demostración', exact: true });
  await add.click();
  await expect(page.getByRole('button', { name: 'Carrito de demostración: 1 producto', exact: true })).toBeVisible();
  await add.click();
  await expect(page.getByRole('button', { name: 'Carrito de demostración: 2 productos', exact: true })).toBeVisible();
  await expect(page.getByRole('status')).toContainText('Simulación: Pack Fiesta Dulce agregado');
  await page.reload();
  await expect(page.getByRole('button', { name: 'Carrito de demostración: 0 productos', exact: true })).toBeVisible();
});

test('inicio muestra la identidad y los cuatro favoritos mock', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('heading', { level: 1, name: 'Antojos para cada momento' })).toBeVisible();
  await expect(page.getByRole('article')).toHaveCount(4);
  for (const name of ['Pack Fiesta Dulce', 'Box Picosita', 'Mix Gomitas', 'Noche de Pelis']) {
    await expect(page.getByRole('heading', { name, exact: true })).toBeVisible();
  }
  await expect(page.getByText('Modo demostración', { exact: false }).first()).toBeVisible();
});
