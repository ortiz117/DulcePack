<script lang="ts">
  import { tick } from 'svelte';
  import Header from '../components/Header.svelte';
  import Footer from '../components/Footer.svelte';
  import Button from '../components/Button.svelte';
  import ProductCard from '../components/ProductCard.svelte';
  import Icon from '../components/Icon.svelte';
  import { categories, featuredProducts } from '../data/home';
  import heroImage from '../assets/fiesta-hero.webp';
  import type { CategoryId, Product } from '../types/product';

  let { navigate }: { navigate: (path: string) => void } = $props();

  // Estado efímero de esta vista. No crea pedidos ni persiste un carrito real.
  let categoryFilter = $state<CategoryId | ''>('');
  let query = $state('');
  let searchOpen = $state(false);
  let searchInput: HTMLInputElement;
  const normalized = (value: string) => value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('es-MX').trim();
  let filteredProducts = $derived(featuredProducts.filter(product =>
    (!categoryFilter || product.category === categoryFilter) &&
    normalized(`${product.name} ${product.description}`).includes(normalized(query))
  ));
  async function toggleSearch() {
    searchOpen = !searchOpen;
    await tick();
    if (searchOpen) searchInput?.focus();
    else document.getElementById('search-trigger')?.focus();
  }
  function resetFilters() {
    categoryFilter = '';
    query = '';
  }
  let cartCount = $state(0);
  let notice = $state('');
  function showPending(view: string) {
    notice = `${view}: disponible en una próxima vista. Esta demostración solo incluye Inicio.`;
  }
  function addDemoProduct(product: Product) {
    cartCount += 1;
    notice = `Simulación: ${product.name} agregado. Productos en el contador: ${cartCount}. No se ha realizado ninguna compra.`;
  }
</script>

<a class="skip-link" href="#contenido">Saltar al contenido</a>
<div id="inicio"></div>
<Header {cartCount} onpending={showPending} oncatalog={() => navigate('/catalogo')} onsearch={toggleSearch} {searchOpen} />
<main id="contenido" class="container" tabindex="-1">
  <div id="home-search" class="home-search" hidden={!searchOpen}>
    <label for="favorite-search">Buscar entre favoritos</label>
    <div class="search-row"><input id="favorite-search" bind:this={searchInput} type="search" bind:value={query} placeholder="Prueba con gomitas o chocolate" onkeydown={(event) => { if (event.key === 'Escape') void toggleSearch(); }} /><button type="button" onclick={toggleSearch}>Cerrar búsqueda</button></div>
    <p>Busca solo en los cuatro productos de demostración de Inicio.</p>
  </div>
  <section class="hero" id="promociones" aria-labelledby="hero-title">
    <div class="hero-copy">
      <span class="eyebrow"><span class="desktop-text">¡Sabor de la semana!</span><span class="mobile-text">Nuevo</span></span>
      <h1 id="hero-title">Antojos para<br />cada momento</h1>
      <p>Descubre combinaciones de dulces y snacks para compartir, regalar o disfrutar cuando quieras.</p>
      <Button variant="pink" onclick={() => navigate('/catalogo')}><span class="desktop-text">Explorar catálogo</span><span class="mobile-text">Ver catálogo</span></Button>
    </div>
    <img class="hero-image" src={heroImage} alt="Paquete Fiesta morado rodeado de confeti rosa y amarillo" width="590" height="334" fetchpriority="high" />
  </section>

  <section class="categories" id="categorias" aria-labelledby="category-title">
    <div class="section-heading"><h2 id="category-title"><span class="desktop-text">Explora por antojo</span><span class="mobile-text">Categorías</span></h2><a href="#favoritos" onclick={resetFilters}>Ver todo <span aria-hidden="true">→</span></a></div>
    <div class="category-list">
      {#each categories as category}
        <button class="category {category.id}" type="button" aria-pressed={categoryFilter === category.id} onclick={() => categoryFilter = categoryFilter === category.id ? '' : category.id}><span class="desktop-text">{category.name}</span><span class="mobile-text">{category.shortName}</span><small>Descubrir →</small></button>
      {/each}
    </div>
  </section>

  <section class="favorites" id="favoritos" aria-labelledby="favorites-title">
    <div class="section-heading"><h2 id="favorites-title"><span class="desktop-text">Los favoritos de la semana</span><span class="mobile-text">Favoritos</span></h2></div>
    {#if categoryFilter || query}<div class="filter-summary"><span>Favoritos encontrados: {filteredProducts.length}</span><button type="button" onclick={resetFilters}>Mostrar todos los favoritos</button></div>{/if}
    <div class="product-grid">{#each filteredProducts as product (product.id)}<ProductCard {product} onadd={addDemoProduct} mobilePriority />{:else}<p class="empty-state">No encontramos favoritos con esos filtros.</p>{/each}</div>
  </section>

  <aside class="benefits" aria-label="Beneficios previstos de la tienda, sujetos a implementación">
    {#each ['Envío rápido 2–5 días', 'Pago seguro', 'Empaque para regalo', 'Atención cercana'] as benefit}<span><Icon name="check" />{benefit}</span>{/each}
  </aside>
</main>
<Footer />
<div class="notice" class:visible={notice !== ''}>
  <span role="status" aria-live="polite" aria-atomic="true">{notice}</span>
  {#if notice}<button type="button" aria-label="Cerrar aviso" onclick={() => notice = ''}>×</button>{/if}
</div>

<style>
  .home-search { background: white; border: 1px solid #e7dcee; padding: 20px; border-radius: 16px; margin-bottom: 24px; }
  .home-search label { display: block; font-weight: 700; margin-bottom: 8px; }
  .search-row { display: flex; flex-wrap: wrap; gap: 12px; }
  .search-row input { flex: 1 1 220px; width: 100%; min-width: 0; padding: 12px; border: 1px solid var(--muted); border-radius: 10px; color: var(--ink); background: white; }
  .search-row button { border: 0; border-radius: 10px; color: white; background: var(--purple); padding: 12px; min-height: 44px; }
  .home-search p { color: var(--muted); margin-bottom: 0; font-size: 14px; }
  .empty-state { grid-column: 1 / -1; padding: 40px 20px; border: 1px dashed var(--purple); border-radius: 16px; text-align: center; }

  .notice { position: fixed; bottom: 20px; left: 50%; transform: translateX(-50%); width: min(560px, calc(100% - 32px)); background: var(--ink); color: white; padding: 16px 20px; border-radius: 16px; box-shadow: 0 8px 32px #24123a30; z-index: 20; font-size: 14px; opacity: 0; pointer-events: none; }
  .notice.visible { opacity: 1; pointer-events: auto; display: flex; gap: 16px; align-items: center; }
  .notice button { flex-shrink: 0; width: 44px; height: 44px; border-radius: 8px; background: #ffffff15; color: white; border: 0; font-size: 26px; }
  .hero { background: var(--purple); color: white; border-radius: 30px; padding: 48px; display: grid; grid-template-columns: 1fr 1.05fr; gap: 48px; align-items: center; min-height: 430px; }
  .eyebrow { display: inline-flex; background: var(--yellow); color: var(--ink); padding: 6px 12px; font-size: 12px; border-radius: 50px; }
  h1 { font: 600 clamp(32px, 3.7vw, 54px)/1.08 'Fredoka Variable', sans-serif; letter-spacing: -.02em; margin: 18px 0 14px; }
  .hero-copy > p { max-width: 460px; font-size: 16px; line-height: 1.5; margin: 0 0 20px; }
  .hero-image { display: block; width: 100%; height: auto; border-radius: 24px; }
  .section-heading { display: flex; justify-content: space-between; align-items: center; gap: 20px; margin-bottom: 28px; }
  .section-heading h2 { margin: 0; font: 600 28px/1.3 'Fredoka Variable', sans-serif; }
  .section-heading a { color: var(--purple); text-decoration: none; font-size: 14px; font-weight: 700; padding: 12px 0; }
  .categories { margin-top: 30px; }
  .category-list { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
  .category { background: #fce7f3; text-align: left; border: 2px solid transparent; border-radius: 20px; min-height: 120px; padding: 20px 22px; color: var(--ink); transition: border-color .18s, transform .18s; }
  .filter-summary { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; margin: -8px 0 20px; font-size: 14px; }
  .filter-summary button { border: 1px solid var(--purple); border-radius: 10px; padding: 10px 14px; color: var(--purple); background: white; min-height: 44px; }
  .category[aria-pressed="true"] { border-color: var(--purple); }
  .category:hover { border-color: var(--purple); transform: translateY(-2px); }
  .category > span { font: 600 22px 'Fredoka Variable', sans-serif; }
  .category small { display: block; font-size: 13px; color: var(--purple); margin-top: 6px; }
  .category.gomitas { background: #fff4c5; } .category.chocolates { background: var(--lavender); }
  .favorites { margin-top: 32px; }
  .product-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 32px; }
  .benefits { display: flex; align-items: center; gap: 36px; flex-wrap: wrap; margin-top: 32px; min-height: 96px; padding: 24px 30px; border-radius: 20px; background: #fff4c5; }
  .benefits span { display: flex; align-items: center; gap: 6px; font-size: 14px; font-weight: 700; }
  .benefits :global(svg) { width: 16px; height: 16px; }
  .mobile-text { display: none; }
  @media (max-width: 1050px) {
    .hero { padding: 32px; gap: 24px; min-height: 360px; }
    .product-grid { gap: 16px; } .category { padding: 18px 14px; } .category > span { font-size: 20px; }
    .benefits { gap: 20px; }
  }
  @media (min-width: 601px) and (max-width: 850px) { .product-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
  @media (max-width: 600px) {
    .desktop-text { display: none; } .mobile-text { display: inline; }
    .hero { display: block; border-radius: 24px; min-height: 244px; padding: 24px 20px; }
    .hero-image, .hero-copy > p { display: none; }
    .eyebrow { min-width: 124px; justify-content: center; padding: 7px 16px; font-size: 13px; }
    h1 { font-size: 36px; line-height: 1.2; margin: 14px 0 16px; letter-spacing: 0; }
    .hero-copy :global(.button) { width: auto; min-width: 150px; }
    .categories { margin-top: 20px; }
    .section-heading { margin-bottom: 20px; }
    .section-heading h2 { font-size: 24px; }
    .section-heading > a { display: none; }
    .category-list { display: flex; gap: 8px; overflow-x: auto; padding-bottom: 6px; scrollbar-width: thin; }
    .category { flex: 0 0 110px; min-height: 40px; padding: 8px 12px; text-align: center; border-radius: 50px; background: #fff4c5 !important; }
    .category > span { font: 400 13px 'Nunito Variable', sans-serif; }
    .category small { display: none; }
    .favorites { margin-top: 22px; }
    .product-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
    .benefits { padding: 20px; display: grid; grid-template-columns: 1fr; margin-top: 24px; gap: 14px; }
  }
</style>
