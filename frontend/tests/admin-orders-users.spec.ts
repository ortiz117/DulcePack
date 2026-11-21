import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

async function enableAdminSession(page: import('@playwright/test').Page) {
  await page.addInitScript(() => sessionStorage.setItem('dulcepack-admin-session', 'active'));
}

test('pedidos muestra el listado mock fiel', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/pedidos');

  await expect(page.getByRole('heading', { level: 1, name: 'Pedidos' })).toBeVisible();
  const table = page.getByRole('table', { name: 'Pedidos administrados' });
  await expect(table.getByRole('row')).toHaveCount(7);
  await expect(table.getByRole('row', { name: /DP-1048 Isaac Ortiz \$635 6 nov Pendiente/ })).toBeVisible();
  await expect(table.getByRole('row', { name: /DP-1043 Sofía Pérez \$258 4 nov Entregado/ })).toBeVisible();
});

test('usuarios muestra el listado mock fiel', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/usuarios');

  await expect(page.getByRole('heading', { level: 1, name: 'Usuarios' })).toBeVisible();
  const table = page.getByRole('table', { name: 'Usuarios administrados' });
  await expect(table.getByRole('row')).toHaveCount(6);
  await expect(table.getByRole('row', { name: /Isaac Ortiz isaac@email.com Cliente 6 Activo/ })).toBeVisible();
  await expect(table.getByRole('row', { name: /María Admin maria@dulcepack.mx Administrador — Activo/ })).toBeVisible();
  await expect(table.getByRole('row', { name: /Marco Ruiz marco@email.com Cliente 1 Suspendido/ })).toBeVisible();
});

test('usuarios exige sesión y se abre desde la navegación administrativa', async ({ page, isMobile }) => {
  await page.goto('/admin/usuarios');
  await expect(page).toHaveURL(/\/admin$/);

  await enableAdminSession(page);
  await page.goto('/admin/pedidos');
  if (isMobile) await page.getByRole('button', { name: 'Abrir menú administrativo' }).click();
  await page.getByRole('button', { name: 'Usuarios' }).click();

  await expect(page).toHaveURL(/\/admin\/usuarios$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Usuarios' })).toBeVisible();
  if (isMobile) await page.getByRole('button', { name: 'Abrir menú administrativo' }).click();
  await expect(page.getByRole('button', { name: 'Usuarios' })).toHaveAttribute('aria-current', 'page');
});

test('el detalle de pedido entrega feedback accesible', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/pedidos');
  await page.getByRole('button', { name: 'Ver detalle de DP-1048' }).click();
  await expect(page.getByRole('status')).toContainText('Detalle del pedido DP-1048');
});

test('pedidos y usuarios son responsive y cumplen WCAG AA', async ({ page }) => {
  await enableAdminSession(page);
  for (const path of ['/admin/pedidos', '/admin/usuarios']) {
    for (const width of [320, 390, 768, 1024, 1440]) {
      await page.setViewportSize({ width, height: 900 });
      await page.goto(path);
      await page.evaluate(() => document.fonts.ready);
      const dimensions = await page.evaluate(() => ({ content: document.documentElement.scrollWidth, viewport: window.innerWidth }));
      expect(dimensions.content, `${path} desborda a ${width}px`).toBeLessThanOrEqual(dimensions.viewport);
      if (width <= 800) {
        const table = await page.getByRole('region', { name: /Tabla desplazable de (pedidos|usuarios)/ }).evaluate(element => ({ scroll: element.scrollWidth, client: element.clientWidth }));
        expect(table.scroll).toBeGreaterThan(table.client);
        await expect(page.getByText('Desliza horizontalmente para ver todos los datos.')).toBeVisible();
      }
    }

    await page.setViewportSize({ width: 1440, height: 1000 });
    await page.goto(path);
    const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa']).analyze();
    expect(results.violations).toEqual([]);
  }
});
