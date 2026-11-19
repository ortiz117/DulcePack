import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

const adminEmail = 'admin@dulcepack.mx';
const adminPassword = 'DulcePack2026';

async function enableAdminSession(page: import('@playwright/test').Page) {
  await page.addInitScript(() => sessionStorage.setItem('dulcepack-admin-session', 'active'));
}

test('el dashboard redirige al login cuando no existe una sesión administrativa', async ({ page }) => {
  await page.goto('/admin/dashboard');
  await expect(page).toHaveURL(/\/admin$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Acceso administrativo' })).toBeVisible();
});

test('el acceso administrativo valida credenciales mock y abre el dashboard', async ({ page }) => {
  await page.goto('/admin');

  await expect(page.getByRole('heading', { level: 1, name: 'Acceso administrativo' })).toBeVisible();
  await page.getByLabel('Correo').fill('otro@dulcepack.mx');
  await page.getByLabel('Contraseña').fill('incorrecta');
  await page.getByRole('button', { name: 'Entrar al panel' }).click();
  await expect(page.getByRole('alert')).toContainText('Credenciales incorrectas');
  await expect(page).toHaveURL(/\/admin$/);

  await page.getByLabel('Correo').fill(adminEmail);
  await page.getByLabel('Contraseña').fill(adminPassword);
  await page.getByRole('button', { name: 'Entrar al panel' }).click();

  await expect(page).toHaveURL(/\/admin\/dashboard$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Resumen' })).toBeVisible();
  await expect(page.getByText('$24,580')).toBeVisible();
  await expect(page.getByText('128', { exact: true })).toBeVisible();
  await expect(page.getByText('36', { exact: true })).toBeVisible();
});

test('el dashboard ofrece acciones mock sin desarrollar módulos adicionales', async ({ page, isMobile }) => {
  await enableAdminSession(page);
  await page.goto('/admin/dashboard');

  await expect(page.getByRole('heading', { name: 'Ventas semanales' })).toBeVisible();
  await expect(page.getByRole('img', { name: 'Gráfica de ventas semanales' })).toBeVisible();
  await page.getByRole('button', { name: 'Gestionar productos' }).click();
  await expect(page.getByRole('status')).toContainText('Productos estará disponible en su módulo asignado');
  await page.getByRole('button', { name: 'Cerrar aviso' }).click();

  if (isMobile) await page.getByRole('button', { name: 'Abrir menú administrativo' }).click();
  await page.getByRole('button', { name: 'Cerrar sesión' }).click();
  await expect(page).toHaveURL(/\/admin$/);
});

test('la navegación administrativa móvil se abre y cierra', async ({ page, isMobile }) => {
  test.skip(!isMobile, 'Interacción exclusiva del menú móvil');
  await enableAdminSession(page);
  await page.goto('/admin/dashboard');

  const menu = page.getByRole('navigation', { name: 'Administración' });
  await expect(menu).toBeHidden();
  await page.getByRole('button', { name: 'Abrir menú administrativo' }).click();
  await expect(menu).toBeVisible();
  await page.getByRole('button', { name: 'Cerrar menú administrativo', exact: true }).click();
  await expect(menu).toBeHidden();
});

test('login y dashboard administrativos son accesibles y no desbordan', async ({ page }) => {
  await enableAdminSession(page);
  for (const path of ['/admin', '/admin/dashboard']) {
    for (const width of [320, 390, 768, 1024, 1440]) {
      await page.setViewportSize({ width, height: 900 });
      await page.goto(path);
      await page.evaluate(() => document.fonts.ready);
      const dimensions = await page.evaluate(() => ({ content: document.documentElement.scrollWidth, viewport: window.innerWidth }));
      expect(dimensions.content, `${path} desborda a ${width}px`).toBeLessThanOrEqual(dimensions.viewport);
    }

    await page.setViewportSize({ width: 1440, height: 1000 });
    await page.goto(path);
    const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa']).analyze();
    expect(results.violations).toEqual([]);
  }
});
