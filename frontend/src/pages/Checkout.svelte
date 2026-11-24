<script lang="ts">
  import Header from '../components/Header.svelte';
  import Footer from '../components/Footer.svelte';
  import type { CartItem } from '../types/product';

  let { cart, cartCount, subtotal, shipping, discount, orderTotal, navigate, completeOrder }: {
    cart: CartItem[];
    cartCount: number;
    subtotal: number;
    shipping: number;
    discount: number;
    orderTotal: number;
    navigate: (path: string) => void;
    completeOrder: () => void;
  } = $props();
  let notice = $state('');

  function submit(event: SubmitEvent) {
    event.preventDefault();
    completeOrder();
  }
</script>

<a class="skip-link" href="#contenido">Saltar al contenido</a>
<Header {cartCount} active="catalogo" oncatalog={() => navigate('/catalogo')} oncart={() => navigate('/carrito')} onpending={(view) => notice = `${view}: disponible próximamente.`} onsearch={() => navigate('/catalogo')} searchOpen={false} />
<main id="contenido" class="container" tabindex="-1">
  <h1>Finalizar pedido</h1>
  <ol class="steps" aria-label="Progreso del pedido"><li>1&nbsp; Datos</li><li>2&nbsp; Envío</li><li aria-current="step">3&nbsp; Pago</li></ol>
  {#if cart.length}
    <form onsubmit={submit}>
      <section class="form-card" aria-labelledby="delivery-title">
        <h2 id="delivery-title">Datos de entrega</h2>
        <div class="fields">
          <label class="full-name">Nombre completo<input required autocomplete="name" placeholder="Isaac Ortiz" /></label>
          <label class="address">Dirección<input required autocomplete="street-address" placeholder="Calle y número" /></label>
          <label>Ciudad<input autocomplete="address-level2" placeholder="Ciudad" /></label>
          <label>Código postal<input inputmode="numeric" autocomplete="postal-code" pattern={'[0-9]{5}'} placeholder="00000" /></label>
        </div>
        <h2>Método de pago</h2>
        <div class="fields">
          <label class="card-number">Número de tarjeta<input required inputmode="numeric" autocomplete="cc-number" pattern={'[0-9 ]{15,19}'} placeholder="4242 4242 4242 4242" /></label>
          <label>Vencimiento<input autocomplete="cc-exp" pattern={'[0-9]{2}/[0-9]{2}'} placeholder="MM/AA" /></label>
          <label>CVV<input inputmode="numeric" autocomplete="cc-csc" pattern={'[0-9]{3,4}'} placeholder="123" /></label>
        </div>
        <p class="demo">Pago de demostración: no se procesan tarjetas ni cargos reales.</p>
      </section>
      <aside class="order-summary">
        <h2>Tu pedido</h2><p>{cartCount} {cartCount === 1 ? 'producto' : 'productos'}</p>
        <dl><div><dt>Subtotal</dt><dd>${subtotal}</dd></div><div><dt>Envío</dt><dd>${shipping}</dd></div><div><dt>Descuento</dt><dd>−${discount}</dd></div><div class="total"><dt>Total</dt><dd>${orderTotal} MXN</dd></div></dl>
        <button type="submit" aria-label={`Pagar $${orderTotal} MXN`}>Pagar ${orderTotal} MXN</button>
        <small>🔒 Tu pago de demostración está protegido</small>
      </aside>
    </form>
  {:else}
    <section class="empty"><h2>No hay productos para finalizar.</h2><button type="button" onclick={() => navigate('/catalogo')}>Ir al catálogo</button></section>
  {/if}
</main>
<Footer />
{#if notice}<div class="notice" role="status">{notice}</div>{/if}

<style>
  h1 { margin: 34px 0 28px; font: 600 38px 'Fredoka Variable', sans-serif; }
  .steps { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; max-width: 900px; margin: 0 0 28px; padding: 0; list-style: none; }
  .steps li { padding: 12px; border-radius: 50px; background: var(--lavender); color: var(--muted); text-align: center; }
  .steps li[aria-current] { background: var(--purple); color: white; }
  form { display: grid; grid-template-columns: minmax(0, 2fr) minmax(310px, .95fr); gap: 48px; align-items: start; min-height: 760px; }
  .form-card { padding: 32px 28px; background: white; }
  h2 { margin: 0 0 22px; font: 600 27px 'Fredoka Variable', sans-serif; }
  .form-card h2:not(:first-child) { margin-top: 26px; }
  .fields { display: grid; grid-template-columns: 1fr 1fr; gap: 22px 20px; }
  label { display: grid; gap: 8px; color: var(--muted); font-size: 13px; }
  .full-name, .card-number { grid-column: 1 / -1; }
  input { min-height: 54px; padding: 0 16px; border: 1px solid #dfd2e8; border-radius: 12px; color: var(--ink); background: white; }
  input:focus { border-color: var(--purple); }
  .demo { margin-top: 24px; color: var(--muted); font-size: 13px; }
  .order-summary { padding: 32px 28px; border-radius: 20px; background: #fff4c5; }
  .order-summary dl div { display: flex; justify-content: space-between; gap: 20px; margin: 20px 0; }
  dd { margin: 0; }
  .total { color: var(--purple); font-size: 21px; font-weight: 800; }
  .order-summary button, .empty button { min-height: 48px; padding: 0 32px; border: 0; border-radius: 12px; background: var(--purple); color: white; font-weight: 800; }
  .order-summary small { display: block; margin-top: 16px; color: var(--muted); }
  .empty { min-height: 560px; padding: 60px; border-radius: 20px; background: white; text-align: center; }
  .notice { position: fixed; left: 50%; bottom: 20px; transform: translateX(-50%); padding: 14px 20px; border-radius: 14px; background: var(--ink); color: white; }
  @media (max-width: 700px) {
    h1 { margin: 24px 0 18px; font-size: 30px; }
    .steps { display: flex; gap: 5px; margin-bottom: 20px; font-size: 13px; }
    .steps li { padding: 0; background: transparent !important; color: var(--purple) !important; }
    .steps li:not(:last-child)::after { content: ' ·'; }
    form { display: block; min-height: 720px; }
    .form-card { padding: 0; background: transparent; }
    .form-card h2, .demo { display: none; }
    .fields { display: block; }
    label { margin-bottom: 24px; }
    label:not(.full-name):not(.address):not(.card-number) { display: none; }
    input { min-height: 56px; }
    .order-summary { padding: 4px 0 0; background: transparent; }
    .order-summary h2, .order-summary p, .order-summary dl div:not(.total), .order-summary small { display: none; }
    .order-summary dl .total { justify-content: flex-start; gap: 22px; font-size: 22px; }
    .order-summary button { min-width: 180px; }
  }
</style>
