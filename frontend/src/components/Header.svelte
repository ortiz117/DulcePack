<script lang="ts">
  import Icon from './Icon.svelte';
  let { cartCount = 0, onpending, onsearch, searchOpen }: { cartCount?: number; onpending: (view: string) => void; onsearch: () => void; searchOpen: boolean } = $props();
</script>

<header class="site-header container">
  <a class="brand" href="#inicio" aria-label="DulcePack, inicio"><span class="brand-dot" aria-hidden="true"></span>DulcePack</a>
  <nav aria-label="Navegación principal">
    <a href="#inicio" aria-current="page">Inicio</a>
    <button type="button" onclick={() => onpending('Catálogo')}>Catálogo</button>
    <a href="#promociones">Promociones</a>
  </nav>
  <div class="header-actions">
    <button id="search-trigger" class="search" type="button" aria-label="Buscar antojos" aria-expanded={searchOpen} aria-controls="home-search" onclick={onsearch}><Icon name="search" /><span>Buscar antojos</span></button>
    <button class="account" type="button" onclick={() => onpending('Mi cuenta')}><span>Mi cuenta</span><Icon name="user" /></button>
    <button class="cart" type="button" onclick={() => onpending('Carrito')} aria-label={`Carrito de demostración: ${cartCount} ${cartCount === 1 ? 'producto' : 'productos'}`}><Icon name="cart" /><span class="cart-count">{cartCount}</span></button>
  </div>
</header>

<style>
  .site-header { display: flex; align-items: center; gap: 24px; min-height: 84px; padding: 16px 24px; margin-top: 24px; margin-bottom: 32px; background: white; border-radius: 16px; box-shadow: var(--shadow); }
  .brand { display: flex; align-items: center; gap: 10px; color: var(--purple); font: 600 22px 'Fredoka Variable', sans-serif; text-decoration: none; white-space: nowrap; }
  .brand-dot { width: 34px; height: 34px; background: var(--pink); border-radius: 50%; }
  nav { display: flex; align-items: center; gap: 24px; }
  nav a, nav button, .account { color: var(--ink); font-size: 14px; font-weight: 600; text-decoration: none; white-space: nowrap; }
  nav a[aria-current] { color: var(--purple); }
  .header-actions { display: flex; align-items: center; gap: 20px; margin-left: auto; }
  button { border: 0; background: none; padding: 8px 0; min-height: 44px; }
  .search { display: flex; align-items: center; gap: 10px; min-width: 220px; padding: 8px 16px; border-radius: 12px; background: var(--lavender); color: var(--muted); font-size: 13px; text-align: left; }
  .account :global(svg) { display: none; }
  .cart { display: flex; align-items: center; gap: 8px; color: var(--purple); }
  .cart-count { display: grid; place-items: center; min-width: 24px; height: 24px; padding: 0 5px; border-radius: 50px; background: var(--yellow); color: var(--ink); font-weight: 700; font-size: 12px; }
  @media (max-width: 1050px) { .search { min-width: auto; background: transparent; padding: 8px; } .search span { display: none; } nav { gap: 16px; } .site-header { gap: 20px; } }
  @media (max-width: 760px) { nav { display: none; } .account { display: none; } }
  @media (max-width: 600px) {
    .site-header { margin-top: 16px; margin-bottom: 20px; min-height: 64px; padding: 8px 16px; border-radius: 18px; }
    .brand { font-size: 24px; } .brand-dot { display: none; }
    .header-actions { gap: 4px; } .search { padding: 8px; color: var(--muted); }
    .search :global(svg) { width: 18px; height: 18px; }
    .cart { gap: 4px; } .cart :global(svg) { width: 18px; height: 18px; }
    .cart-count { background: none; color: var(--purple); min-width: 12px; padding: 0; font-size: 14px; }
  }
</style>
