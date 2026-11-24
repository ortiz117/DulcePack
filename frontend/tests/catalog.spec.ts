import { test, expect } from '@playwright/test';

test('Catálogo abre una vista propia con todos los productos', async ({ page, isMobile }) => {
  await page.goto('/');
  await page.getByRole('button', { name: isMobile ? 'Ver catálogo' : 'Catálogo', exact: true }).click();

  await expect(page).toHaveURL(/\/catalogo$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Catálogo de antojos' })).toBeVisible();
  await expect(page.getByRole('article')).toHaveCount(6);
});

test('los filtros de categoría y precio se pueden combinar y restablecer', async ({ page, isMobile }) => {
  test.skip(isMobile, 'El diseño móvil utiliza categorías rápidas.');
  await page.goto('/catalogo');

  await page.getByRole('checkbox', { name: 'Botanas' }).check();
  await expect(page.getByRole('article')).toHaveCount(2);
  await page.getByRole('checkbox', { name: 'Más de $200' }).check();
  await expect(page.getByRole('article')).toHaveCount(1);
  await expect(page.getByRole('heading', { name: 'Noche de Pelis', exact: true })).toBeVisible();

  await page.getByRole('button', { name: 'Limpiar filtros' }).click();
  await expect(page.getByRole('article')).toHaveCount(6);
});

test('la búsqueda del encabezado filtra productos y se puede cerrar con Escape', async ({ page }) => {
  await page.goto('/catalogo');

  await page.getByRole('button', { name: 'Buscar antojos', exact: true }).click();
  const search = page.getByRole('searchbox', { name: 'Buscar productos' });
  await expect(search).toBeFocused();
  await search.fill('PICOSITA');
  await expect(page.getByRole('article')).toHaveCount(1);
  await expect(page.getByRole('heading', { name: 'Box Picosita', exact: true })).toBeVisible();
  await expect(page.getByRole('heading', { level: 1, name: 'Catálogo de antojos' })).not.toBeVisible();
  await expect(page.getByRole('complementary', { name: 'Filtros del catálogo' })).not.toBeVisible();
  await expect(page.getByRole('group', { name: 'Categorías rápidas' })).not.toBeVisible();
  await search.fill('');
  await expect(page.getByRole('heading', { level: 1, name: 'Catálogo de antojos' })).toBeVisible();
  await search.press('Escape');
  await expect(search).not.toBeVisible();
});

test('en móvil las categorías rápidas sustituyen al panel lateral', async ({ page, isMobile }) => {
  test.skip(!isMobile, 'Comportamiento exclusivo del catálogo móvil.');
  await page.goto('/catalogo');

  await expect(page.getByRole('button', { name: 'Todos', exact: true })).toHaveAttribute('aria-pressed', 'true');
  await page.getByRole('button', { name: 'Botanas', exact: true }).click();
  await expect(page.getByRole('article')).toHaveCount(2);
  await expect(page.getByRole('button', { name: 'Botanas', exact: true })).toHaveAttribute('aria-pressed', 'true');
});

test('un producto abre su detalle y agrega la cantidad seleccionada', async ({ page }) => {
  await page.goto('/catalogo');
  await page.getByRole('link', { name: 'Ver Pack Fiesta Dulce' }).click();

  await expect(page).toHaveURL(/\/producto\/fiesta$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Pack Fiesta Dulce' })).toBeVisible();
  const quantity = page.getByLabel('Cantidad seleccionada');
  await expect(quantity).toHaveText('1');
  await page.getByRole('button', { name: 'Aumentar cantidad' }).click();
  await page.getByRole('button', { name: 'Aumentar cantidad' }).click();
  await expect(quantity).toHaveText('3');
  await page.getByRole('button', { name: 'Agregar 3 Pack Fiesta Dulce al carrito' }).click();
  await expect(page.getByRole('button', { name: 'Carrito de demostración: 3 productos' })).toBeVisible();
});
