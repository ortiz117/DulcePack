import { test, expect } from '@playwright/test';

test('Mi cuenta abre el inicio de sesión y permite entrar en modo demostración', async ({ page, isMobile }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Mi cuenta', exact: true }).click();

  await expect(page).toHaveURL(/\/iniciar-sesion$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Bienvenido de nuevo' })).toBeVisible();
  await page.getByLabel('Correo electrónico').fill('julia@dulcepack.test');
  await page.getByLabel('Contraseña').fill('dulcepack123');
  await page.getByRole('button', { name: 'Iniciar sesión', exact: true }).click();

  await expect(page).toHaveURL(/\/$/);
  if (!isMobile) await expect(page.getByRole('button', { name: 'Cuenta de julia' })).toBeVisible();
});

test('el registro valida la confirmación de contraseña y crea una sesión', async ({ page, isMobile }) => {
  await page.goto('/iniciar-sesion');
  await page.getByRole('button', { name: '¿No tienes cuenta? Regístrate' }).click();

  await expect(page).toHaveURL(/\/registro$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Crea tu cuenta' })).toBeVisible();
  await page.getByLabel('Nombre completo').fill('Julia Ortiz');
  await page.getByLabel('Correo electrónico').fill('julia@dulcepack.test');
  await page.getByLabel('Contraseña', { exact: true }).fill('dulcepack123');
  await page.getByLabel('Confirmar contraseña').fill('otra-clave');
  await page.getByRole('button', { name: 'Crear cuenta' }).click();
  await expect(page.getByRole('status')).toContainText('Las contraseñas no coinciden');

  await page.getByLabel('Confirmar contraseña').fill('dulcepack123');
  await page.getByRole('button', { name: 'Crear cuenta' }).click();
  await expect(page).toHaveURL(/\/$/);
  if (!isMobile) await expect(page.getByRole('button', { name: 'Cuenta de Julia' })).toBeVisible();
});
