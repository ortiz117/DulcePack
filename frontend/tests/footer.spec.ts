import { test, expect } from '@playwright/test';

test('el pie ofrece navegación funcional para cliente y administración', async ({ page }) => {
  await page.goto('/catalogo');
  const footer = page.getByRole('contentinfo');

  await expect(footer.getByRole('link', { name: 'Favoritos' })).toHaveAttribute('href', '/#favoritos');
  await expect(footer.getByRole('link', { name: 'Categorías' })).toHaveAttribute('href', '/#categorias');

  await footer.getByRole('link', { name: 'Carrito' }).click();
  await expect(page).toHaveURL(/\/carrito$/);
  await page.goBack();

  await footer.getByRole('link', { name: 'Administración' }).click();
  await expect(page).toHaveURL(/\/admin$/);
  await expect(page.getByRole('heading', { name: 'Acceso administrativo' })).toBeVisible();
});