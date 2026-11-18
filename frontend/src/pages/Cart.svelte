<script lang="ts">
  import Header from '../components/Header.svelte';
  import Footer from '../components/Footer.svelte';
  import type { CartItem } from '../types/product';

  let { cart, cartCount, navigate, updateCart, removeFromCart }: {
    cart: CartItem[];
    cartCount: number;
    navigate: (path: string) => void;
    updateCart: (productId: string, quantity: number) => void;
    removeFromCart: (productId: string) => void;
  } = $props();

  let coupon = $state('DULCE50');
  let notice = $state('');
  let subtotal = $derived(cart.reduce((total, item) => total + item.product.price * item.quantity, 0));
  let shipping = $derived(cart.length ? 79 : 0);
  let discount = $derived(coupon.trim().toUpperCase() === 'DULCE50' && cart.length ? 50 : 0);
  let total = $derived(Math.max(0, subtotal + shipping - discount));
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
  <h1>Tu carrito</h1>
  {#if cart.length}
    <div class="cart-layout">
      <section class="items" aria-label="Productos en el carrito">
        {#each cart as item (item.product.id)}
          <article aria-label={item.product.name}>
            <div class="art"><span aria-hidden="true"></span></div>
            <div class="item-copy">
              <h2>{item.product.name}</h2>
              <div class="quantity"><span>Cantidad:</span><button type="button" aria-label={`Disminuir ${item.product.name}`} onclick={() => updateCart(item.product.id, item.quantity - 1)} disabled={item.quantity === 1}>−</button><strong>{item.quantity}</strong><button type="button" aria-label={`Aumentar ${item.product.name}`} onclick={() => updateCart(item.product.id, item.quantity + 1)}>+</button></div>
            </div>
            <div class="item-price"><strong>${item.product.price * item.quantity} MXN</strong><button type="button" aria-label={`Eliminar ${item.product.name}`} onclick={() => removeFromCart(item.product.id)}>Eliminar</button></div>
          </article>
        {/each}
      </section>
      <aside class="summary" aria-label="Resumen del carrito">
        <h2>Resumen</h2>
        <dl><div><dt>Subtotal</dt><dd>${subtotal}</dd></div><div><dt>Envío</dt><dd>${shipping}</dd></div><div class="discount"><dt>Descuento</dt><dd>−${discount}</dd></div><div class="total"><dt>Total</dt><dd>${total} MXN</dd></div></dl>
        <label for="coupon">Cupón</label><input id="coupon" bind:value={coupon} />
        <button class="checkout" type="button" onclick={() => navigate('/checkout')}>Continuar al pago</button>
      </aside>
    </div>
  {:else}
    <section class="empty"><h2>Tu carrito está vacío.</h2><p>Explora el catálogo y agrega tus antojos favoritos.</p><button type="button" onclick={() => navigate('/catalogo')}>Ir al catálogo</button></section>
  {/if}
</main>
<Footer />
{#if notice}<div class="notice" role="status">{notice}</div>{/if}

<style>
  h1 { margin: 34px 0 30px; font: 600 38px 'Fredoka Variable', sans-serif; }
  .cart-layout { display: grid; grid-template-columns: minmax(0, 2fr) minmax(300px, .95fr); gap: 48px; align-items: start; min-height: 680px; }
  .items { display: grid; gap: 16px; }
  article { display: grid; grid-template-columns: 118px minmax(0, 1fr) auto; gap: 20px; align-items: center; min-height: 150px; padding: 16px; border-radius: 18px; background: white; box-shadow: var(--shadow); }
  .art { display: grid; place-items: center; height: 118px; border-radius: 14px; background: #fce7f3; }
  .art span { display: block; width: 60px; height: 84px; border-radius: 9px; background: var(--purple); }
  article h2 { margin: 0 0 14px; font: 600 22px 'Fredoka Variable', sans-serif; }
  .quantity { display: flex; align-items: center; gap: 12px; color: var(--muted); }
  .quantity button { width: 30px; height: 30px; border: 0; background: transparent; color: var(--muted); font-size: 18px; }
  .item-price { display: grid; gap: 16px; justify-items: start; min-width: 120px; }
  .item-price strong { color: var(--purple); font-size: 20px; }
  .item-price button { border: 0; background: transparent; color: #b91c1c; padding: 0; }
  .summary { padding: 32px 28px; border-radius: 18px; background: white; box-shadow: 0 16px 34px #24123a12; }
  .summary h2 { margin: 0 0 22px; font: 600 28px 'Fredoka Variable', sans-serif; }
  dl { margin: 0; }
  dl div { display: flex; justify-content: space-between; gap: 20px; margin: 20px 0; }
  dd { margin: 0; }
  .discount { color: #15803d; }
  .total { color: var(--purple); font-size: 21px; font-weight: 800; }
  .summary label { display: block; margin: 24px 0 8px; color: var(--muted); font-size: 13px; }
  .summary input { width: 100%; min-height: 54px; padding: 0 16px; border: 1px solid #dfd2e8; border-radius: 12px; }
  .checkout, .empty button { min-height: 48px; margin-top: 24px; padding: 0 30px; border: 0; border-radius: 12px; background: var(--purple); color: white; font-weight: 800; }
  .empty { min-height: 520px; padding: 64px; border-radius: 22px; background: white; text-align: center; }
  .empty h2 { font: 600 30px 'Fredoka Variable', sans-serif; }
  .notice { position: fixed; left: 50%; bottom: 20px; transform: translateX(-50%); padding: 14px 20px; border-radius: 14px; background: var(--ink); color: white; }
  @media (max-width: 700px) {
    h1 { margin: 24px 0 20px; font-size: 30px; }
    .cart-layout { display: block; min-height: 700px; }
    .items { gap: 20px; }
    article { grid-template-columns: 72px minmax(0, 1fr) auto; min-height: 96px; padding: 12px; gap: 12px; box-shadow: none; }
    .art { height: 72px; }
    .art span { display: none; }
    article h2 { margin-bottom: 8px; font: 700 14px 'Nunito Variable', sans-serif; }
    .quantity { gap: 9px; font-size: 13px; }
    .quantity > span { display: none; }
    .quantity button { width: 20px; height: 24px; font-size: 15px; }
    .item-price { min-width: auto; justify-items: end; }
    .item-price strong { font-size: 15px; white-space: nowrap; }
    .item-price button { font-size: 12px; }
    .summary { padding: 24px 0 0; background: transparent; box-shadow: none; }
    .summary h2, .summary dl div:not(.total), .summary label, .summary input { display: none; }
    .summary dl .total { justify-content: flex-start; gap: 22px; margin: 0; font-size: 22px; }
    .checkout { min-width: 180px; }
    .empty { min-height: 580px; padding: 48px 22px; }
  }
</style>
