<footer class="site-footer container">
  <div class="footer-brand"><a href="#inicio">DulcePack</a><p>Una tienda digital donde los antojos encuentran su lugar.</p></div>
  <div><h2>Tienda</h2><a href="#favoritos">Favoritos</a><a href="#categorias">Categorías</a></div>
  <div><h2>Tu compra</h2><p>Carrito y pedidos<br />Próximamente</p></div>
  <div><h2>Hecho para compartir</h2><p>Dulces, snacks y muchos buenos momentos.</p></div>
  <p class="demo-note">Modo demostración · Productos y precios de ejemplo. Sin compras ni pagos reales. El contador se reinicia al recargar.</p>
</footer>

<style>
  .site-footer { margin-top: 32px; margin-bottom: 24px; padding: 36px 48px 24px; display: grid; grid-template-columns: 1.7fr 1fr 1fr 1fr; gap: 28px; background: var(--purple); border-radius: 20px; color: white; }
  .footer-brand > a { font: 600 28px 'Fredoka Variable', sans-serif; }
  h2 { font: 500 18px 'Fredoka Variable', sans-serif; margin: 0 0 12px; }
  p, a { color: inherit; font-size: 14px; line-height: 1.6; }
  p { margin: 8px 0 0; } a { display: block; text-decoration: none; margin-bottom: 8px; } a:hover { text-decoration: underline; }
  .demo-note { grid-column: 1 / -1; border-top: 1px solid #ffffff40; padding-top: 16px; font-size: 12px; margin: 0; }
  @media (max-width: 760px) { .site-footer { grid-template-columns: 1fr 1fr; padding: 28px; gap: 24px; } }
  @media (max-width: 600px) { .site-footer { margin-top: 24px; border-radius: 20px; padding: 24px; } .footer-brand { grid-column: 1 / -1; } .site-footer > div:last-of-type { display: none; } }
</style>
