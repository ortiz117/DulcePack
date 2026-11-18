<script lang="ts">
  import Header from '../components/Header.svelte';
  import Footer from '../components/Footer.svelte';
  import ProductCard from '../components/ProductCard.svelte';
  import { catalogProducts } from '../data/home';
  import type { Product } from '../types/product';

  let { productId, navigate, cartCount, addToCart }: { productId: string; navigate: (path: string) => void; cartCount: number; addToCart: (product: Product, quantity?: number) => void } = $props();
  let product = $derived(catalogProducts.find(item => item.id === productId) ?? catalogProducts[0]);
  let quantity = $state(1);

  let notice = $state('');
  let related = $derived(catalogProducts.filter(item => item.id !== product.id).slice(0, 3));

  function addSelected() {
    addToCart(product, quantity);
    notice = `${quantity} ${product.name} agregados al carrito de demostración.`;
  }

  function addRelated(item: Product) {
    addToCart(item);
    notice = `${item.name} agregado al carrito de demostración.`;
  }
</script>

<a class="skip-link" href="#contenido">Saltar al contenido</a>
<Header
  {cartCount}
  active="catalogo"
  oncatalog={() => navigate('/catalogo')}
  oncart={() => navigate('/carrito')}
  onpending={(view) => notice = `${view}: disponible próximamente.`}
  onsearch={() => navigate('/catalogo')}
  searchOpen={false}
/>
<main id="contenido" class="container" tabindex="-1">
  <nav class="breadcrumbs" aria-label="Migas de pan"><a href="/" onclick={(event) => { event.preventDefault(); navigate('/'); }}>Inicio</a><span>/</span><a href="/catalogo" onclick={(event) => { event.preventDefault(); navigate('/catalogo'); }}>Catálogo</a><span>/</span><span>{product.name}</span></nav>
  <section class="detail">
    <div class="detail-art"><img src={product.image} alt={`Ilustración de ${product.name}`} width="590" height="560" /></div>
    <div class="detail-copy">
      <span class="availability">Disponible</span>
      <h1>{product.name}</h1>
      <p class="lead">{product.id === 'fiesta' ? 'Una selección de gomitas, paletas, chocolates y botanas para compartir con amigos.' : product.description}</p>
      <p class="price">${product.price} MXN</p>
      <p class="meta">Incluye aproximadamente 25 piezas · Empaque resellable</p>
      <div class="quantity">
        <strong>Cantidad</strong>
        <button type="button" aria-label="Disminuir cantidad" disabled={quantity === 1} onclick={() => quantity = Math.max(1, quantity - 1)}>−</button>
        <span aria-label="Cantidad seleccionada">{quantity}</span>
        <button type="button" aria-label="Aumentar cantidad" onclick={() => quantity += 1}>+</button>
      </div>
      <button class="add-main" type="button" aria-label={`Agregar ${quantity} ${product.name} al carrito`} onclick={addSelected}>Agregar al carrito</button>
      <p class="benefits">✓ Pago seguro &nbsp; ✓ Envío de 2 a 5 días &nbsp; ✓ Empaque protegido</p>
    </div>
  </section>

  <section class="related" aria-labelledby="related-title">
    <h2 id="related-title">También podría gustarte</h2>
    <div class="related-grid">
      {#each related as item (item.id)}<ProductCard product={item} onadd={addRelated} onselect={(selected) => navigate(`/producto/${selected.id}`)} />{/each}
    </div>
  </section>
</main>
<Footer />
{#if notice}<div class="notice" role="status">{notice}</div>{/if}

<style>
  .breadcrumbs { display: flex; gap: 8px; margin: 34px 0; color: var(--muted); font-size: 14px; }
  .breadcrumbs a { color: inherit; text-decoration: none; }
  .detail { display: grid; grid-template-columns: 1fr 1.15fr; background: white; }
  .detail-art { display: grid; place-items: center; min-height: 560px; padding: 48px; border-radius: 28px; background: #fce7f3; overflow: hidden; }
  .detail-art img { display: block; width: 100%; max-width: 520px; height: auto; border-radius: 24px; }
  .detail-copy { padding: 48px 56px; align-self: center; }
  .availability { display: inline-block; min-width: 124px; padding: 8px 18px; border-radius: 50px; background: var(--yellow); text-align: center; font-weight: 700; font-size: 13px; }
  h1 { margin: 24px 0 18px; font: 600 40px/1.1 'Fredoka Variable', sans-serif; }
  .lead { max-width: 620px; color: var(--muted); font-size: 18px; line-height: 1.6; }
  .price { color: var(--purple); font: 700 30px 'Fredoka Variable', sans-serif; margin: 16px 0; }
  .meta, .benefits { color: var(--muted); font-size: 14px; }
  .quantity { display: flex; align-items: center; gap: 14px; margin: 24px 0 18px; }
  .quantity button { width: 36px; height: 36px; border: 0; background: transparent; color: var(--purple); font-size: 24px; font-weight: 800; }
  .quantity span { color: var(--purple); font-size: 22px; font-weight: 800; }
  .add-main { min-height: 52px; padding: 0 34px; border: 0; border-radius: 12px; background: var(--purple); color: white; font-weight: 800; }
  .related { margin-top: 36px; }
  .related h2 { font: 600 30px 'Fredoka Variable', sans-serif; }
  .related-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 42px; }
  .notice { position: fixed; left: 50%; bottom: 20px; transform: translateX(-50%); padding: 14px 20px; border-radius: 14px; background: var(--ink); color: white; z-index: 20; }

  @media (max-width: 760px) {
    .breadcrumbs, .availability, .meta, .benefits, .related { display: none; }
    .detail { display: block; background: transparent; }
    .detail-art { min-height: 300px; padding: 34px; border-radius: 24px; }
    .detail-art img { max-width: 250px; }
    .detail-copy { padding: 24px 0 70px; }
    h1 { margin: 0 0 22px; font-size: 30px; }
    .lead { font-size: 16px; }
    .price { font-size: 24px; }
    .add-main { min-width: 180px; }
  }
</style>
