import { test, expect } from '@playwright/test';

test('los productos agregados llegan al carrito y permiten cambiar cantidades', async ({ page }) => {
  await page.goto('/producto/fiesta');
  await page.getByRole('button', { name: 'Aumentar cantidad' }).click();
  await page.getByRole('button', { name: 'Agregar 2 Pack Fiesta Dulce al carrito' }).click();
  await page.getByRole('button', { name: 'Carrito de demostración: 2 productos' }).click();

  await expect(page).toHaveURL(/\/carrito$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Tu carrito' })).toBeVisible();
  await expect(page.getByRole('article', { name: 'Pack Fiesta Dulce' })).toContainText('$378 MXN');

  await page.getByRole('button', { name: 'Disminuir Pack Fiesta Dulce' }).click();
  await expect(page.getByRole('article', { name: 'Pack Fiesta Dulce' })).toContainText('$189 MXN');
  await page.getByRole('button', { name: 'Eliminar Pack Fiesta Dulce' }).click();
  await expect(page.getByText('Tu carrito está vacío.')).toBeVisible();
});

test('el checkout valida los datos y confirma un pedido de demostración', async ({ page, isMobile }) => {
  await page.goto('/producto/fiesta');
  await page.getByRole('button', { name: 'Agregar 1 Pack Fiesta Dulce al carrito' }).click();
  await page.getByRole('button', { name: 'Carrito de demostración: 1 producto' }).click();
  await page.getByRole('button', { name: 'Continuar al pago' }).click();

  await expect(page).toHaveURL(/\/checkout$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Finalizar pedido' })).toBeVisible();
  await page.getByLabel('Nombre completo').fill('Julia Ortiz');
  await page.getByLabel('Dirección').fill('Calle Dulce 123');
  await page.getByLabel('Número de tarjeta').fill('4242 4242 4242 4242');
  if (!isMobile) {
    await page.getByLabel('Ciudad').fill('Ciudad de México');
    await page.getByLabel('Código postal').fill('01000');
    await page.getByLabel('Vencimiento').fill('12/30');
    await page.getByLabel('CVV').fill('123');
  }
  await page.getByRole('button', { name: 'Pagar $218 MXN' }).click();

  await expect(page).toHaveURL(/\/confirmacion$/);
  await expect(page.getByRole('heading', { level: 1, name: isMobile ? '¡Pedido listo!' : '¡Pedido confirmado!' })).toBeVisible();
  await expect(page.getByText(/DP-1048/).first()).toBeAttached();
});
