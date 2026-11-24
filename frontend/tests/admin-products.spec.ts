import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

async function enableAdminSession(page: import('@playwright/test').Page) {
  await page.addInitScript(() => sessionStorage.setItem('dulcepack-admin-session', 'active'));
}

test('productos administrativos exige una sesión activa', async ({ page }) => {
  await page.goto('/admin/productos');
  await expect(page).toHaveURL(/\/admin$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Acceso administrativo' })).toBeVisible();
});

test('productos muestra el listado mock fiel y permite buscar por nombre', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/productos');

  await expect(page.getByRole('heading', { level: 1, name: 'Productos' })).toBeVisible();
  const table = page.getByRole('table', { name: 'Productos administrados' });
  await expect(table.getByRole('row')).toHaveCount(7);
  await expect(table.getByText('Pack Fiesta Dulce')).toBeVisible();
  await expect(table.getByText('Choco Lovers')).toBeVisible();

  await page.getByRole('searchbox', { name: 'Buscar producto' }).fill('picosita');
  await expect(table.getByRole('row')).toHaveCount(2);
  await expect(table.getByText('Box Picosita')).toBeVisible();
  await expect(table.getByText('Pack Fiesta Dulce')).toBeHidden();
});

test('nuevo producto y editar navegan a sus formularios correspondientes', async ({ page, isMobile }) => {
  await enableAdminSession(page);
  await page.goto('/admin/productos');

  if (isMobile) await page.getByRole('button', { name: 'Abrir menú administrativo' }).click();
  await expect(page.getByRole('button', { name: 'Productos' })).toHaveAttribute('aria-current', 'page');
  if (isMobile) await page.getByRole('button', { name: 'Cerrar menú administrativo', exact: true }).click();
  await page.getByRole('button', { name: 'Nuevo producto' }).click();
  await expect(page).toHaveURL(/\/admin\/productos\/nuevo$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Nuevo producto' })).toBeVisible();

  await page.goto('/admin/productos');
  await page.getByRole('button', { name: 'Editar Pack Fiesta Dulce' }).click();
  await expect(page).toHaveURL(/\/admin\/productos\/fiesta-dulce\/editar$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Editar producto' })).toBeVisible();
  await expect(page).toHaveTitle('Editar producto | DulcePack');
  await expect(page.getByLabel('Nombre')).toHaveValue('Pack Fiesta Dulce');
  await expect(page.getByLabel('Descripción')).toHaveValue('Dulces variados para compartir');
  await expect(page.getByLabel('Precio')).toHaveValue('189');
  await expect(page.getByLabel('Stock')).toHaveValue('24');
  await expect(page.getByLabel('Categoría')).toHaveValue('Paquetes');
  await expect(page.getByLabel('Etiquetas')).toHaveValue('Fiesta, Compartir, Más vendido');
  await expect(page.getByRole('button', { name: 'Guardar cambios' })).toBeVisible();
});

test('productos es responsive y cumple las comprobaciones automáticas WCAG AA', async ({ page }) => {
  await enableAdminSession(page);
  for (const width of [320, 390, 768, 1024, 1440]) {
    await page.setViewportSize({ width, height: 900 });
    await page.goto('/admin/productos');
    await page.evaluate(() => document.fonts.ready);
    const dimensions = await page.evaluate(() => ({ content: document.documentElement.scrollWidth, viewport: window.innerWidth }));
    expect(dimensions.content, `productos desborda a ${width}px`).toBeLessThanOrEqual(dimensions.viewport);
    if (width <= 800) {
      const tableScroll = await page.getByRole('region', { name: 'Tabla desplazable de productos' }).evaluate(element => ({ scroll: element.scrollWidth, client: element.clientWidth }));
      expect(tableScroll.scroll).toBeGreaterThan(tableScroll.client);
    }
  }

  await page.setViewportSize({ width: 1440, height: 1100 });
  await page.goto('/admin/productos');
  const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa']).analyze();
  expect(results.violations).toEqual([]);
});
