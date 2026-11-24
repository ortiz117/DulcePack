import { test, expect } from '@playwright/test';

test('la marca usa el símbolo y el nombre como imágenes separadas', async ({ page }) => {
  await page.goto('/');

  const brand = page.getByRole('banner').getByRole('link', { name: 'DulcePack, inicio' });
  await expect(brand).toBeVisible();
  await expect(brand.locator('img[data-brand-part="mark"]')).toHaveCount(1);
  await expect(brand.locator('img[data-brand-part="wordmark"]')).toHaveCount(1);
  await expect(brand.locator('img[data-brand-part="mark"]')).toHaveAttribute('alt', '');
  await expect(brand.locator('img[data-brand-part="wordmark"]')).toHaveAttribute('alt', 'DulcePack');
});

test('la identidad visual separada también aparece en los accesos', async ({ page }) => {
  for (const route of ['/iniciar-sesion', '/registro', '/admin']) {
    await page.goto(route);
    const brand = page.getByRole('link', { name: 'DulcePack, inicio' });
    await expect(brand).toBeVisible();
    await expect(brand.locator('img[data-brand-part="mark"]')).toBeVisible();
    await expect(brand.locator('img[data-brand-part="wordmark"]')).toBeVisible();
  }
});

test('la marca conserva proporciones y no desborda en escritorio ni móvil', async ({ page }) => {
  await page.goto('/');

  const brand = page.getByRole('banner').getByRole('link', { name: 'DulcePack, inicio' });
  for (const width of [1440, 390]) {
    await page.setViewportSize({ width, height: 900 });
    const metrics = await brand.evaluate((element) => {
      const rect = element.getBoundingClientRect();
      const mark = element.querySelector<HTMLImageElement>('img[data-brand-part="mark"]');
      const wordmark = element.querySelector<HTMLImageElement>('img[data-brand-part="wordmark"]');
      return {
        left: rect.left,
        right: rect.right,
        viewport: document.documentElement.clientWidth,
        markLoaded: Boolean(mark?.complete && mark.naturalWidth > 0),
        wordmarkLoaded: Boolean(wordmark?.complete && wordmark.naturalWidth > 0),
      };
    });

    expect(metrics.markLoaded).toBe(true);
    expect(metrics.wordmarkLoaded).toBe(true);
    expect(metrics.left).toBeGreaterThanOrEqual(0);
    expect(metrics.right).toBeLessThanOrEqual(metrics.viewport);
  }
});
