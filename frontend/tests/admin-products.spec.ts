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

test('nuevo producto y editar permanecen como acciones mock fuera de alcance', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/productos');

  await page.getByRole('button', { name: 'Nuevo producto' }).click();
  await expect(page.getByRole('status')).toContainText('Nuevo producto se implementará en su vista asignada');
  await page.getByRole('button', { name: 'Cerrar aviso' }).click();

  await page.getByRole('button', { name: 'Editar Pack Fiesta Dulce' }).click();
  await expect(page.getByRole('status')).toContainText('La edición de Pack Fiesta Dulce estará disponible');
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
