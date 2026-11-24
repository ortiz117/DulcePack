import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

async function enableAdminSession(page: import('@playwright/test').Page) {
  await page.addInitScript(() => sessionStorage.setItem('dulcepack-admin-session', 'active'));
}

test('nuevo producto exige una sesión administrativa activa', async ({ page }) => {
  await page.goto('/admin/productos/nuevo');

  await expect(page).toHaveURL(/\/admin$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Acceso administrativo' })).toBeVisible();
});

test('nuevo producto muestra el formulario con sus valores iniciales exactos', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/productos/nuevo');

  await expect(page.getByRole('heading', { level: 1, name: 'Nuevo producto' })).toBeVisible();
  await expect(page.getByRole('heading', { level: 2, name: 'Información del producto' })).toBeVisible();
  await expect(page.getByRole('heading', { level: 2, name: 'Imagen del producto' })).toBeVisible();
  await expect(page.getByLabel('Nombre')).toHaveValue('Pack Fiesta Dulce');
  await expect(page.getByLabel('Descripción')).toHaveValue('Dulces variados para compartir');
  await expect(page.getByLabel('Precio')).toHaveValue('189');
  await expect(page.getByLabel('Stock')).toHaveValue('24');
  await expect(page.getByLabel('Categoría')).toHaveValue('Paquetes');
  await expect(page.getByLabel('Etiquetas')).toHaveValue('Fiesta, Compartir, Más vendido');
  await expect(page.getByRole('button', { name: 'Guardar producto' })).toBeVisible();
  await expect(page.getByText('Arrastra una imagen o haz clic para subir')).toBeVisible();
  await expect(page.locator('input[type="file"]')).toHaveAttribute('accept', 'image/*');
});

test('nuevo producto limpia la confirmación al editar antes de un envío inválido', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/productos/nuevo');

  const price = page.getByLabel('Precio');
  const saveStatus = page.getByRole('status');
  await page.getByRole('button', { name: 'Guardar producto' }).click();
  await expect(saveStatus).toHaveText('Producto guardado en modo demostración. Los cambios no son persistentes.');

  await price.fill('0');
  await expect(saveStatus).toHaveCount(0);

  await page.getByRole('button', { name: 'Guardar producto' }).click();
  await expect(price).toHaveJSProperty('validity.valid', false);
  await expect(saveStatus).toHaveCount(0);
});

test('nuevo producto anuncia el nombre de la imagen seleccionada localmente y limpia la confirmación previa', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/productos/nuevo');

  const saveStatus = page.getByText('Producto guardado en modo demostración. Los cambios no son persistentes.');
  await page.getByRole('button', { name: 'Guardar producto' }).click();
  await expect(saveStatus).toBeVisible();

  await page.locator('input[type="file"]').setInputFiles({
    name: 'pack-fiesta.png',
    mimeType: 'image/png',
    buffer: Buffer.from('imagen de prueba'),
  });

  await expect(saveStatus).toHaveCount(0);
  await expect(page.locator('[aria-label="Archivo seleccionado"]')).toHaveText('pack-fiesta.png');
});

test('nuevo producto rechaza una selección que no es imagen, limpia el nombre y la confirmación', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/productos/nuevo');

  const fileInput = page.locator('input[type="file"]');
  await fileInput.setInputFiles({
    name: 'pack-fiesta.png',
    mimeType: 'image/png',
    buffer: Buffer.from('imagen de prueba'),
  });
  await expect(page.locator('[aria-label="Archivo seleccionado"]')).toHaveText('pack-fiesta.png');

  const saveStatus = page.getByText('Producto guardado en modo demostración. Los cambios no son persistentes.');
  await page.getByRole('button', { name: 'Guardar producto' }).click();
  await expect(saveStatus).toBeVisible();
  await expect(page.getByRole('status')).toHaveCount(1);

  await fileInput.setInputFiles({
    name: 'notas.txt',
    mimeType: 'text/plain',
    buffer: Buffer.from('texto de prueba'),
  });

  await expect(saveStatus).toHaveCount(0);
  await expect(page.locator('[aria-label="Archivo seleccionado"]')).toHaveCount(0);
  await expect(page.getByRole('alert')).toHaveText('Selecciona un archivo de imagen válido.');

  await fileInput.setInputFiles({
    name: 'pack-recuperado.webp',
    mimeType: 'image/webp',
    buffer: Buffer.from('imagen válida'),
  });

  await expect(page.getByRole('alert')).toHaveCount(0);
  await expect(page.locator('[aria-label="Archivo seleccionado"]')).toHaveText('pack-recuperado.webp');
});

test('nuevo producto acepta una imagen arrastrada, anuncia su nombre y limpia la confirmación previa', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/productos/nuevo');

  const saveStatus = page.getByText('Producto guardado en modo demostración. Los cambios no son persistentes.');
  await page.getByRole('button', { name: 'Guardar producto' }).click();
  await expect(saveStatus).toBeVisible();

  const uploadZone = page.locator('.upload-zone');
  const dataTransfer = await page.evaluateHandle(() => {
    const transfer = new DataTransfer();
    transfer.items.add(new File(['imagen de prueba'], 'pack-arrastrado.png', { type: 'image/png' }));
    return transfer;
  });

  await uploadZone.dispatchEvent('dragenter', { dataTransfer });
  await expect(uploadZone).toHaveClass(/is-dragging/);
  await uploadZone.dispatchEvent('dragover', { dataTransfer });
  const dropPrevented = await uploadZone.evaluate((element, transfer) => {
    const event = new DragEvent('drop', { bubbles: true, cancelable: true, dataTransfer: transfer as DataTransfer });
    element.dispatchEvent(event);
    return event.defaultPrevented;
  }, dataTransfer);

  expect(dropPrevented).toBe(true);
  await expect(uploadZone).not.toHaveClass(/is-dragging/);
  await expect(saveStatus).toHaveCount(0);
  await expect(page.locator('[aria-label="Archivo seleccionado"]')).toHaveText('pack-arrastrado.png');
});

test('nuevo producto rechaza un archivo arrastrado que no es imagen, limpia el nombre y la confirmación', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/productos/nuevo');

  await page.locator('input[type="file"]').setInputFiles({
    name: 'pack-fiesta.png',
    mimeType: 'image/png',
    buffer: Buffer.from('imagen de prueba'),
  });
  await expect(page.locator('[aria-label="Archivo seleccionado"]')).toHaveText('pack-fiesta.png');

  const saveStatus = page.getByText('Producto guardado en modo demostración. Los cambios no son persistentes.');
  await page.getByRole('button', { name: 'Guardar producto' }).click();
  await expect(saveStatus).toBeVisible();

  const uploadZone = page.locator('.upload-zone');
  const dataTransfer = await page.evaluateHandle(() => {
    const transfer = new DataTransfer();
    transfer.items.add(new File(['texto de prueba'], 'notas.txt', { type: 'text/plain' }));
    return transfer;
  });
  const dropPrevented = await uploadZone.evaluate((element, transfer) => {
    const event = new DragEvent('drop', { bubbles: true, cancelable: true, dataTransfer: transfer as DataTransfer });
    element.dispatchEvent(event);
    return event.defaultPrevented;
  }, dataTransfer);

  expect(dropPrevented).toBe(true);
  await expect(saveStatus).toHaveCount(0);
  await expect(page.locator('[aria-label="Archivo seleccionado"]')).toHaveCount(0);
  await expect(page.getByRole('alert')).toHaveText('Selecciona un archivo de imagen válido.');

  const validDataTransfer = await page.evaluateHandle(() => {
    const transfer = new DataTransfer();
    transfer.items.add(new File(['imagen válida'], 'pack-recuperado.png', { type: 'image/png' }));
    return transfer;
  });
  await uploadZone.dispatchEvent('drop', { dataTransfer: validDataTransfer });

  await expect(page.getByRole('alert')).toHaveCount(0);
  await expect(page.locator('[aria-label="Archivo seleccionado"]')).toHaveText('pack-recuperado.png');
});

test('nuevo producto usa dos columnas en escritorio y no desborda entre 320 y 1440 píxeles', async ({ page }) => {
  await enableAdminSession(page);

  for (const width of [320, 390, 768, 1024, 1440]) {
    await page.setViewportSize({ width, height: 1000 });
    await page.goto('/admin/productos/nuevo');
    await page.evaluate(() => document.fonts.ready);
    const dimensions = await page.evaluate(() => ({ content: document.documentElement.scrollWidth, viewport: window.innerWidth }));
    expect(dimensions.content, `nuevo producto desborda a ${width}px`).toBeLessThanOrEqual(dimensions.viewport);
  }

  await page.setViewportSize({ width: 768, height: 1000 });
  await page.goto('/admin/productos/nuevo');
  const tabletFormBox = await page.getByRole('region', { name: 'Información del producto' }).boundingBox();
  const tabletImageBox = await page.getByRole('complementary', { name: 'Imagen del producto' }).boundingBox();
  expect(tabletFormBox).not.toBeNull();
  expect(tabletImageBox).not.toBeNull();
  expect(tabletImageBox!.y).toBeGreaterThan(tabletFormBox!.y + tabletFormBox!.height);

  await page.setViewportSize({ width: 1440, height: 1000 });
  await page.goto('/admin/productos/nuevo');
  const formBox = await page.getByRole('region', { name: 'Información del producto' }).boundingBox();
  const imageBox = await page.getByRole('complementary', { name: 'Imagen del producto' }).boundingBox();
  expect(formBox).not.toBeNull();
  expect(imageBox).not.toBeNull();
  expect(imageBox!.x).toBeGreaterThan(formBox!.x + formBox!.width);
});

test('nuevo producto cumple WCAG A y AA en estado inicial y con archivo seleccionado', async ({ page }) => {
  await enableAdminSession(page);
  await page.goto('/admin/productos/nuevo');

  const scan = async () => new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa']).analyze();
  expect((await scan()).violations).toEqual([]);

  await page.locator('input[type="file"]').setInputFiles({
    name: 'pack-fiesta.png',
    mimeType: 'image/png',
    buffer: Buffer.from('imagen de prueba'),
  });
  expect((await scan()).violations).toEqual([]);
});
