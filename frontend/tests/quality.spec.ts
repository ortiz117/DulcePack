import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

test('inicio no tiene errores de consola, recursos rotos ni enlaces internos inválidos', async ({ page }) => {
  const failures: string[] = [];
  page.on('pageerror', error => failures.push(error.message));
  page.on('console', message => { if (message.type() === 'error') failures.push(message.text()); });
  page.on('response', response => { if (response.status() >= 400) failures.push(`${response.status()} ${response.url()}`); });
  await page.goto('/');
  await page.evaluate(() => document.fonts.ready);
  await page.locator('footer').scrollIntoViewIfNeeded();
  // Las imágenes lazy ocultas por el diseño móvil no se cargan hasta ser visibles.
  await expect.poll(() => page.locator('img:visible').evaluateAll(images => images.every(image => image instanceof HTMLImageElement && image.complete && image.naturalWidth > 0))).toBe(true);
  const imageUrls = await page.locator('img').evaluateAll(images => [...new Set(images.map(image => (image as HTMLImageElement).src))]);
  for (const url of imageUrls) expect((await page.request.get(url)).ok()).toBe(true);
  const invalidLinks = await page.locator('a[href^="#"]').evaluateAll(links => links.filter(link => !document.getElementById(link.getAttribute('href')!.slice(1))).map(link => link.textContent));
  expect(invalidLinks).toEqual([]);
  expect(failures).toEqual([]);
});

test('inicio cumple las comprobaciones automáticas de accesibilidad WCAG AA', async ({ page }) => {
  await page.goto('/');
  const scan = async () => new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa']).analyze();
  expect((await scan()).violations).toEqual([]);
  await page.getByRole('button', { name: 'Buscar antojos', exact: true }).click();
  await page.getByRole('searchbox').fill('sin-coincidencias');
  expect((await scan()).violations).toEqual([]);
});

test('inicio se adapta sin desbordamiento horizontal de 320 a 1920 píxeles', async ({ page }) => {
  for (const width of [320, 390, 768, 1024, 1440, 1920]) {
    await page.setViewportSize({ width, height: 900 });
    await page.goto('/');
    await page.evaluate(() => document.fonts.ready);
    const dimensions = await page.evaluate(() => ({ content: document.documentElement.scrollWidth, viewport: window.innerWidth }));
    expect(dimensions.content, `desbordamiento a ${width}px`).toBeLessThanOrEqual(dimensions.viewport);
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible();
  }
});

test('el enlace de salto permite llegar al contenido con teclado', async ({ page }) => {
  await page.goto('/');
  await page.keyboard.press('Tab');
  await expect(page.getByRole('link', { name: 'Saltar al contenido' })).toBeFocused();
  await page.keyboard.press('Enter');
  await expect(page.getByRole('main')).toBeFocused();
});
