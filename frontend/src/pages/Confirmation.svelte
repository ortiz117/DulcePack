<script lang="ts">
  import Header from '../components/Header.svelte';
  let { navigate, cartCount }: { navigate: (path: string) => void; cartCount: number } = $props();
  let notice = $state('');
</script>

<a class="skip-link" href="#contenido">Saltar al contenido</a>
<Header {cartCount} active="catalogo" oncatalog={() => navigate('/catalogo')} oncart={() => navigate('/carrito')} onpending={(view) => notice = `${view}: disponible próximamente.`} onsearch={() => navigate('/catalogo')} searchOpen={false} />
<main id="contenido" class="container confirmation-wrap" tabindex="-1">
  <section class="confirmation">
    <div class="check" aria-hidden="true">✓</div>
    <h1><span class="desktop-title">¡Pedido confirmado!</span><span class="mobile-title">¡Pedido listo!</span></h1>
    <p><span class="desktop-title">Tu pedido DP-1048 está en preparación. Recibirás actualizaciones por correo.</span><span class="mobile-title">Recibirás actualizaciones de tu pedido DP-1048.</span></p>
    <strong class="estimate">Entrega estimada: 10–12 de noviembre</strong>
    <button type="button" onclick={() => navigate('/')}>Volver al inicio</button>
  </section>
</main>
{#if notice}<div class="notice" role="status">{notice}</div>{/if}

<style>
  .confirmation-wrap { min-height: 800px; }
  .confirmation { width: min(900px, 100%); min-height: 560px; margin-top: 28px; padding: 56px; border-radius: 28px; background: white; box-shadow: 0 18px 38px #24123a12; text-align: center; }
  .check { display: grid; place-items: center; width: 96px; height: 96px; margin: 0 auto 24px; border-radius: 50%; background: #22c55e; color: white; font-size: 54px; }
  h1 { margin: 0 0 22px; font: 600 38px 'Fredoka Variable', sans-serif; }
  p { color: var(--muted); font-size: 18px; }
  .estimate { display: block; margin: 24px 0; color: var(--purple); font-size: 21px; }
  button { min-height: 48px; padding: 0 44px; border: 0; border-radius: 12px; background: var(--purple); color: white; font-weight: 800; }
  .mobile-title { display: none; }
  .notice { position: fixed; left: 50%; bottom: 20px; transform: translateX(-50%); padding: 14px 20px; border-radius: 14px; background: var(--ink); color: white; }
  @media (max-width: 700px) {
    .confirmation-wrap { min-height: 760px; }
    .confirmation { min-height: 520px; margin-top: 20px; padding: 40px 28px; box-shadow: none; }
    .check { width: 80px; height: 80px; margin-bottom: 24px; font-size: 44px; }
    h1 { font-size: 30px; }
    p { text-align: left; font-size: 16px; line-height: 1.6; }
    .estimate { display: none; }
    .desktop-title { display: none; }
    .mobile-title { display: inline; }
    button { margin-top: 4px; min-width: 180px; }
  }
</style>
