import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

async function enableAdminSession(page: import('@playwright/test').Page) {
  await page.addInitScript(() => sessionStorage.setItem('dulcepack-admin-session', 'active'));
}

test('inventario exige sesión y se abre desde la navegación administrativa', async ({ page, isMobile }) => {
  await page.goto('/admin/inventario');
  await expect(page).toHaveURL(/\/admin$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Acceso administrativo' })).toBeVisible();

  await enableAdminSession(page);
  await page.goto('/admin/productos');
  if (isMobile) await page.getByRole('button', { name: 'Abrir menú administrativo' }).click();
  await page.getByRole('button', { name: 'Inventario', exact: true }).click();

  await expect(page).toHaveURL(/\/admin\/inventario$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Inventario' })).toBeVisible();
  if (isMobile) await page.getByRole('button', { name: 'Abrir menú administrativo' }).click();
  await expect(page.getByRole('button', { name: 'Inventario' })).toHaveAttribute('aria-current', 'page');
});

test('inventario muestra métricas y existencias exactas sin acciones inventadas', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/inventario');

  const metrics = page.getByRole('region', { name: 'Resumen de inventario' });
  await expect(metrics.getByText('Unidades disponibles')).toBeVisible();
  await expect(metrics.getByText('436', { exact: true })).toBeVisible();
  await expect(metrics.getByText('Stock bajo')).toBeVisible();
  await expect(metrics.getByText('3', { exact: true })).toBeVisible();
  await expect(metrics.getByText('Sin existencias')).toBeVisible();
  await expect(metrics.getByText('1', { exact: true })).toBeVisible();

  const table = page.getByRole('table', { name: 'Inventario de productos' });
  await expect(table.getByRole('columnheader')).toHaveText(['Producto', 'Disponible', 'Mínimo', 'Estado', 'Actualización']);
  await expect(table.getByRole('row')).toHaveCount(6);
  await expect(table.getByRole('row', { name: 'Pack Fiesta Dulce 24 10 Disponible Hoy, 08:30' })).toBeVisible();
  await expect(table.getByRole('row', { name: 'Box Picosita 7 10 Bajo Ayer' })).toBeVisible();
  await expect(table.getByRole('row', { name: 'Mix Gomitas 38 12 Disponible Hoy, 07:10' })).toBeVisible();
  await expect(table.getByRole('row', { name: 'Pack Cumpleaños 4 8 Bajo Ayer' })).toBeVisible();
  await expect(table.getByRole('row', { name: 'Choco Lovers 18 8 Disponible 2 nov' })).toBeVisible();
  await expect(page.getByRole('searchbox')).toHaveCount(0);
  await expect(page.getByRole('button', { name: /nuevo|editar|actualizar/i })).toHaveCount(0);
});

test('inventario reemplaza el placeholder del dashboard por navegación real', async ({ page, isMobile }) => {
  await enableAdminSession(page);
  await page.goto('/admin/dashboard');
  if (isMobile) await page.getByRole('button', { name: 'Abrir menú administrativo' }).click();
  await page.getByRole('button', { name: 'Inventario', exact: true }).click();
  await expect(page).toHaveURL(/\/admin\/inventario$/);
});

test('inventario adapta métricas y tabla sin desbordar y cumple WCAG AA', async ({ page }) => {
  await enableAdminSession(page);
  for (const width of [320, 390, 768, 1024, 1440]) {
    await page.setViewportSize({ width, height: 1000 });
    await page.goto('/admin/inventario');
    await page.evaluate(() => document.fonts.ready);

    const dimensions = await page.evaluate(() => ({ content: document.documentElement.scrollWidth, viewport: window.innerWidth }));
    expect(dimensions.content, `inventario desborda a ${width}px`).toBeLessThanOrEqual(dimensions.viewport);

    if (width <= 800) {
      const table = await page.getByRole('region', { name: 'Tabla desplazable de inventario' }).evaluate(element => ({ scroll: element.scrollWidth, client: element.clientWidth }));
      expect(table.scroll).toBeGreaterThan(table.client);
      await expect(page.getByText('Desliza horizontalmente para ver todos los datos.')).toBeVisible();
    }

    const cards = page.getByRole('region', { name: 'Resumen de inventario' }).locator('article');
    if (width === 390) {
      const boxes = await cards.evaluateAll(elements => elements.map(element => element.getBoundingClientRect().top));
      expect(new Set(boxes).size).toBe(3);
    }
    if (width === 768) {
      const boxes = await cards.evaluateAll(elements => elements.map(element => element.getBoundingClientRect().top));
      expect(boxes[0]).toBe(boxes[1]);
      expect(boxes[2]).toBeGreaterThan(boxes[1]);
    }
  }

  await page.setViewportSize({ width: 1440, height: 1000 });
  await page.goto('/admin/inventario');
  const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa']).analyze();
  expect(results.violations).toEqual([]);
});
