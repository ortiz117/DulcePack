<script lang="ts">
  import { tick } from 'svelte';
  import Header from '../components/Header.svelte';
  import Footer from '../components/Footer.svelte';
  import ProductCard from '../components/ProductCard.svelte';
  import { catalogProducts } from '../data/home';
  import type { Product } from '../types/product';

  let { navigate, cartCount, addToCart }: { navigate: (path: string) => void; cartCount: number; addToCart: (product: Product, quantity?: number) => void } = $props();
  let notice = $state('');
  let kinds = $state<string[]>([]);
  let flavors = $state<string[]>([]);
  let prices = $state<string[]>([]);
  let query = $state('');
  let searchOpen = $state(false);
  let mobileCategory = $state<'todos' | 'dulces' | 'botanas'>('todos');
  let searchInput: HTMLInputElement;
  const normalize = (value: string) => value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('es-MX').trim();
  let searchActive = $derived(normalize(query) !== '');
  let filteredProducts = $derived(catalogProducts.filter(product => {
    const kindMatches = kinds.length === 0 || (product.kind && kinds.includes(product.kind));
    const flavorMatches = flavors.length === 0 || (product.flavor && flavors.includes(product.flavor));
    const priceRange = product.price < 150 ? 'under' : product.price <= 200 ? 'middle' : 'over';
    const queryMatches = normalize(`${product.name} ${product.description}`).includes(normalize(query));
    return kindMatches && flavorMatches && queryMatches && (prices.length === 0 || prices.includes(priceRange));
  }));

  async function toggleSearch() {
    searchOpen = !searchOpen;
    await tick();
    if (searchOpen) searchInput?.focus();
    else document.getElementById('search-trigger')?.focus();
  }

  function toggleFilter(group: string[], value: string, checked: boolean) {
    return checked ? [...group, value] : group.filter(item => item !== value);
  }

  function clearFilters() {
    kinds = [];
    flavors = [];
    prices = [];
    query = '';
    mobileCategory = 'todos';
  }

  function setMobileCategory(category: 'todos' | 'dulces' | 'botanas') {
    mobileCategory = category;
    kinds = category === 'todos' ? [] : category === 'dulces' ? ['dulces', 'paquetes'] : ['botanas'];
  }

  function addProduct(product: Product) {
    addToCart(product);
    notice = `${product.name} agregado al carrito de demostración.`;
  }

  function openProduct(product: Product) {
    navigate(`/producto/${product.id}`);
  }
</script>

<a class="skip-link" href="#contenido">Saltar al contenido</a>
<Header
  {cartCount}
  active="catalogo"
  oncatalog={() => navigate('/catalogo')}
  oncart={() => navigate('/carrito')}
  onpending={(view) => notice = `${view}: disponible próximamente.`}
  onsearch={toggleSearch}
  {searchOpen}
  searchControls="catalog-search"
/>
<main id="contenido" class="container" tabindex="-1">
  <div id="catalog-search" class="catalog-search" hidden={!searchOpen}>
    <label for="product-search">Buscar productos</label>
    <div><input id="product-search" bind:this={searchInput} type="search" bind:value={query} placeholder="Busca por nombre o descripción" onkeydown={(event) => { if (event.key === 'Escape') void toggleSearch(); }} /><button type="button" onclick={toggleSearch}>Cerrar</button></div>
  </div>
  <header class="catalog-heading" hidden={searchActive}>
    <h1>Catálogo de antojos</h1>
    <p>Filtra por tipo, sabor o presupuesto y encuentra tu paquete ideal.</p>
  </header>
  <div class="mobile-categories" aria-label="Categorías rápidas" hidden={searchActive}>
    <button type="button" aria-pressed={mobileCategory === 'todos'} onclick={() => setMobileCategory('todos')}>Todos</button>
    <button type="button" aria-pressed={mobileCategory === 'dulces'} onclick={() => setMobileCategory('dulces')}>Dulces</button>
    <button type="button" aria-pressed={mobileCategory === 'botanas'} onclick={() => setMobileCategory('botanas')}>Botanas</button>
  </div>
  <div class:search-mode={searchActive} class="catalog-layout">
    <aside aria-label="Filtros del catálogo" hidden={searchActive}>
      <h2>Filtros</h2>
      <fieldset>
        <legend>Categoría</legend>
        {#each [['dulces', 'Dulces'], ['botanas', 'Botanas'], ['paquetes', 'Paquetes']] as option}
          <label><input type="checkbox" checked={kinds.includes(option[0])} onchange={(event) => kinds = toggleFilter(kinds, option[0], event.currentTarget.checked)} /> {option[1]}</label>
        {/each}
      </fieldset>
      <fieldset>
        <legend>Sabor</legend>
        {#each [['dulce', 'Dulce'], ['acido', 'Ácido'], ['picante', 'Picante']] as option}
          <label><input type="checkbox" checked={flavors.includes(option[0])} onchange={(event) => flavors = toggleFilter(flavors, option[0], event.currentTarget.checked)} /> {option[1]}</label>
        {/each}
      </fieldset>
      <fieldset>
        <legend>Precio</legend>
        {#each [['under', 'Menos de $150'], ['middle', '$150–$200'], ['over', 'Más de $200']] as option}
          <label><input type="checkbox" checked={prices.includes(option[0])} onchange={(event) => prices = toggleFilter(prices, option[0], event.currentTarget.checked)} /> {option[1]}</label>
        {/each}
      </fieldset>
      <button class="clear" type="button" onclick={clearFilters}>Limpiar filtros</button>
    </aside>
    <section class:search-results={searchActive} class="product-grid" aria-label="Productos del catálogo">
      {#each filteredProducts as product (product.id)}
        <ProductCard {product} onadd={addProduct} onselect={openProduct} />
      {:else}
        <p class="empty">No encontramos productos con esos filtros.</p>
      {/each}
    </section>
  </div>
</main>
<Footer />
{#if notice}<div class="notice" role="status">{notice}</div>{/if}

<style>
  .catalog-heading { margin: 34px 0 28px; }
  .catalog-search { margin: 28px 0 0; padding: 20px; border-radius: 16px; background: white; box-shadow: var(--shadow); }
  .catalog-search label { display: block; margin-bottom: 8px; font-weight: 800; }
  .catalog-search div { display: flex; gap: 12px; }
  .catalog-search input { flex: 1; min-width: 0; padding: 12px 14px; border: 1px solid #e7dcee; border-radius: 10px; }
  .catalog-search button { min-height: 44px; border: 0; border-radius: 10px; padding: 0 18px; background: var(--purple); color: white; font-weight: 700; }
  h1 { margin: 0 0 12px; font: 600 36px/1.15 'Fredoka Variable', sans-serif; }
  .catalog-heading p { color: var(--muted); }
  .catalog-layout { display: grid; grid-template-columns: 220px minmax(0, 1fr); gap: 36px; align-items: start; }
  .catalog-layout.search-mode { display: block; }
  .mobile-categories { display: none; }
  aside { min-height: 520px; padding: 24px; border-radius: 16px; background: white; box-shadow: var(--shadow); }
  aside h2 { margin-top: 0; font: 600 20px 'Fredoka Variable', sans-serif; }
  fieldset { border: 0; padding: 0; margin: 22px 0; }
  legend { color: var(--purple); font-weight: 800; font-size: 13px; margin-bottom: 10px; }
  label { display: block; margin: 10px 0; color: var(--muted); font-size: 14px; }
  input { accent-color: var(--purple); }
  .clear { width: 100%; min-height: 44px; border: 1px solid var(--purple); border-radius: 10px; background: white; color: var(--purple); font-weight: 700; }
  .product-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 28px; }
  .product-grid.search-results { grid-template-columns: repeat(auto-fit, minmax(min(240px, 100%), 320px)); }
  .empty { grid-column: 1 / -1; padding: 48px 20px; border: 1px dashed var(--purple); border-radius: 16px; text-align: center; }
  .notice { position: fixed; left: 50%; bottom: 20px; transform: translateX(-50%); padding: 14px 20px; border-radius: 14px; background: var(--ink); color: white; z-index: 20; }
  @media (max-width: 900px) { .product-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
  @media (max-width: 600px) {
    .catalog-heading { margin: 24px 0 20px; }
    h1 { font-size: 30px; }
    .catalog-heading p, aside { display: none; }
    .mobile-categories { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin: 0 0 20px; }
    .mobile-categories button { min-height: 44px; border: 0; border-radius: 50px; background: var(--lavender); color: var(--muted); }
    .mobile-categories button[aria-pressed="true"] { background: var(--purple); color: white; }
    .catalog-layout { display: block; }
    .product-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
  }
</style>