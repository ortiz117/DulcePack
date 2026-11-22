import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

async function enableAdminSession(page: import('@playwright/test').Page) {
  await page.addInitScript(() => sessionStorage.setItem('dulcepack-admin-session', 'active'));
}

test('un id de producto desconocido reemplaza la ruta y atrás no vuelve a atraparse en ella', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/dashboard');
  await page.evaluate(() => {
    history.pushState({}, '', '/admin/productos/no-existe/editar');
    window.dispatchEvent(new PopStateEvent('popstate'));
  });

  await expect(page).toHaveURL(/\/admin\/productos$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Productos' })).toBeVisible();

  await page.goBack();
  await expect(page).toHaveURL(/\/admin\/dashboard$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Resumen' })).toBeVisible();
});

for (const malformedPath of [
  '/admin/productos//editar',
  '/admin/productos/fiesta-dulce/editar/',
  '/admin/productos/fiesta-dulce/editar/extra',
]) {
  test(`la ruta malformada ${malformedPath} se reemplaza por el listado`, async ({ page }) => {
    await enableAdminSession(page);
    await page.goto(malformedPath);

    await expect(page).toHaveURL(/\/admin\/productos$/);
    await expect(page.getByRole('heading', { level: 1, name: 'Productos' })).toBeVisible();
  });
}

test('editar producto exige una sesión administrativa activa', async ({ page }) => {
  await page.goto('/admin/productos/fiesta-dulce/editar');

  await expect(page).toHaveURL(/\/admin$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Acceso administrativo' })).toBeVisible();
});

test('editar otra fila precarga los datos del producto seleccionado', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/productos');
  await page.getByRole('button', { name: 'Editar Box Picosita' }).click();

  await expect(page).toHaveURL(/\/admin\/productos\/box-picosita\/editar$/);
  await expect(page.getByLabel('Nombre')).toHaveValue('Box Picosita');
  await expect(page.getByLabel('Descripción')).toHaveValue('Selección demostrativa de Box Picosita');
  await expect(page.getByLabel('Precio')).toHaveValue('159');
  await expect(page.getByLabel('Stock')).toHaveValue('7');
  await expect(page.getByLabel('Categoría')).toHaveValue('Paquetes');
  await expect(page.getByLabel('Etiquetas')).toHaveValue('Demo, Paquetes');
});

test('cambiar entre categorías de edición selecciona la categoría de cada producto sin estado residual', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/productos/mix-gomitas/editar');

  const category = page.getByLabel('Categoría');
  await expect(category).toHaveValue('Dulces');
  await category.selectOption('Paquetes');

  await page.evaluate(() => {
    history.pushState({}, '', '/admin/productos/noche-pelis/editar');
    window.dispatchEvent(new PopStateEvent('popstate'));
  });
  await expect(page.getByLabel('Nombre')).toHaveValue('Noche de Pelis');
  await expect(page.getByLabel('Categoría')).toHaveValue('Botanas');

  await page.evaluate(() => {
    history.pushState({}, '', '/admin/productos/mix-gomitas/editar');
    window.dispatchEvent(new PopStateEvent('popstate'));
  });
  await expect(page.getByLabel('Nombre')).toHaveValue('Mix Gomitas');
  await expect(page.getByLabel('Categoría')).toHaveValue('Dulces');
});

test('editar valida y confirma el guardado demostrativo sin conservar estados obsoletos', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/productos/fiesta-dulce/editar');

  const price = page.getByLabel('Precio');
  const saveStatus = page.getByRole('status');
  await page.getByRole('button', { name: 'Guardar cambios' }).click();
  await expect(saveStatus).toHaveText('Cambios guardados en modo demostración. Los cambios no son persistentes.');

  await price.fill('0');
  await expect(saveStatus).toHaveCount(0);
  await page.getByRole('button', { name: 'Guardar cambios' }).click();
  await expect(price).toHaveJSProperty('validity.valid', false);
  await expect(saveStatus).toHaveCount(0);

  await price.fill('199');
  await page.getByRole('button', { name: 'Guardar cambios' }).click();
  await expect(saveStatus).toBeVisible();
  await page.locator('input[type="file"]').setInputFiles({
    name: 'edicion.png',
    mimeType: 'image/png',
    buffer: Buffer.from('imagen de edición'),
  });
  await expect(saveStatus).toHaveCount(0);
  await expect(page.locator('[aria-label="Archivo seleccionado"]')).toHaveText('edicion.png');
});

test('cambiar entre ids de edición reinicia los estados transitorios de la instancia', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/productos/fiesta-dulce/editar');

  await page.locator('input[type="file"]').setInputFiles({
    name: 'fiesta.png',
    mimeType: 'image/png',
    buffer: Buffer.from('imagen fiesta'),
  });
  await page.getByRole('button', { name: 'Guardar cambios' }).click();
  await page.locator('.upload-zone').dispatchEvent('dragenter');
  await expect(page.getByRole('status')).toBeVisible();
  await expect(page.locator('[aria-label="Archivo seleccionado"]')).toHaveText('fiesta.png');
  await expect(page.locator('.upload-zone')).toHaveClass(/is-dragging/);

  await page.evaluate(() => {
    history.pushState({}, '', '/admin/productos/box-picosita/editar');
    window.dispatchEvent(new PopStateEvent('popstate'));
  });

  await expect(page.getByLabel('Nombre')).toHaveValue('Box Picosita');
  await expect(page.getByRole('status')).toHaveCount(0);
  await expect(page.locator('[aria-label="Archivo seleccionado"]')).toHaveCount(0);
  await expect(page.locator('.upload-zone')).not.toHaveClass(/is-dragging/);

  await page.locator('input[type="file"]').setInputFiles({
    name: 'not-image.txt',
    mimeType: 'text/plain',
    buffer: Buffer.from('no es imagen'),
  });
  await expect(page.getByRole('alert')).toHaveText('Selecciona un archivo de imagen válido.');

  await page.evaluate(() => {
    history.pushState({}, '', '/admin/productos/fiesta-dulce/editar');
    window.dispatchEvent(new PopStateEvent('popstate'));
  });

  await expect(page.getByLabel('Nombre')).toHaveValue('Pack Fiesta Dulce');
  await expect(page.getByRole('alert')).toHaveCount(0);
});

test('editar producto es responsive y cumple WCAG AA', async ({ page }) => {
  await enableAdminSession(page);
  for (const width of [320, 390, 768, 1024, 1440]) {
    await page.setViewportSize({ width, height: 1000 });
    await page.goto('/admin/productos/fiesta-dulce/editar');
    await page.evaluate(() => document.fonts.ready);
    const dimensions = await page.evaluate(() => ({ content: document.documentElement.scrollWidth, viewport: window.innerWidth }));
    expect(dimensions.content, `editar producto desborda a ${width}px`).toBeLessThanOrEqual(dimensions.viewport);
  }

  await page.setViewportSize({ width: 1440, height: 1000 });
  await page.goto('/admin/productos/fiesta-dulce/editar');
  const scan = async () => new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa']).analyze();
  expect((await scan()).violations).toEqual([]);

  await page.locator('input[type="file"]').setInputFiles({
    name: 'edicion.png',
    mimeType: 'image/png',
    buffer: Buffer.from('imagen de edición'),
  });
  expect((await scan()).violations).toEqual([]);
});
