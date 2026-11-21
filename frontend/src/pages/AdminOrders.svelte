<script lang="ts">
  import AdminShell from '../components/AdminShell.svelte';
  import { adminOrders } from '../data/admin';

  let { navigate }: { navigate: (path: string) => void } = $props();
  let notice = $state('');
</script>

<svelte:head><title>Pedidos administrativos | DulcePack</title></svelte:head>

<AdminShell title="Pedidos" active="Pedidos" {navigate}>
  <section class="table-panel" aria-label="Listado de pedidos">
    <p class="scroll-hint">Desliza horizontalmente para ver todos los datos.</p>
    <!-- svelte-ignore a11y_no_noninteractive_tabindex (región desplazable accesible por teclado) -->
    <div class="table-scroll" role="region" aria-label="Tabla desplazable de pedidos" tabindex="0">
      <table aria-label="Pedidos administrados">
        <thead><tr><th scope="col">Pedido</th><th scope="col">Cliente</th><th scope="col">Total</th><th scope="col">Fecha</th><th scope="col">Estado</th><th scope="col">Acciones</th></tr></thead>
        <tbody>
          {#each adminOrders as order (order.id)}
            <tr>
              <th scope="row">{order.id}</th><td>{order.customer}</td><td>${order.total}</td><td>{order.date}</td>
              <td><span class:pending={order.status === 'Pendiente'} class:cancelled={order.status === 'Cancelado'} class="status-badge">{order.status}</span></td>
              <td><button type="button" aria-label={`Ver detalle de ${order.id}`} onclick={() => notice = `Detalle del pedido ${order.id}.`}>Ver detalle</button></td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  </section>
</AdminShell>

{#if notice}<div class="toast" role="status">{notice}<button type="button" aria-label="Cerrar aviso" onclick={() => notice = ''}>×</button></div>{/if}

<style>
  .table-panel { margin-top: 35px; min-height: 760px; padding: 20px; overflow: hidden; border-radius: 18px; background: white; box-shadow: 0 5px 20px rgba(38,22,50,.06); }
  .table-scroll { width: 100%; overflow-x: auto; border-radius: 12px; scrollbar-gutter: stable; -webkit-overflow-scrolling: touch; }
  .scroll-hint { display: none; }
  table { width: 100%; min-width: 850px; border-collapse: collapse; table-layout: fixed; color: #6b5a7a; font-size: 14px; }
  th, td { height: 66px; padding: 0 16px; border-bottom: 1px solid #e8def2; text-align: left; font-weight: 500; }
  thead th { height: 48px; border: 0; color: #6b5a7a; background: #f7f3ff; font-size: 12px; font-weight: 700; }
  thead th:first-child { border-radius: 12px 0 0 12px; } thead th:last-child { border-radius: 0 12px 12px 0; }
  tbody th { color: var(--ink); font-weight: 700; }
  .status-badge { min-width: 112px; min-height: 30px; padding: 0 12px; display: inline-grid; place-items: center; border-radius: 999px; color: #54258a; background: #eee4ff; font-size: 12px; font-weight: 700; }
  .status-badge.pending { color: #725100; background: #fff1b8; }
  .status-badge.cancelled { color: #a51f5d; background: #fce7f3; }
  td button { min-height: 44px; padding: 0; border: 0; color: #6b5a7a; background: transparent; font-weight: 700; }
  td button:hover { color: var(--purple); text-decoration: underline; }
  .toast { position: fixed; right: 24px; bottom: 24px; z-index: 50; width: min(420px, calc(100% - 32px)); display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 15px 18px; border-radius: 14px; color: white; background: var(--ink); box-shadow: 0 15px 35px rgba(36,18,58,.22); }
  .toast button { border: 0; color: white; background: transparent; font-size: 24px; }
  @media (max-width: 800px) { .table-panel { margin-top: 20px; min-height: 620px; padding: 14px; } .scroll-hint { display: block; margin: 0 0 10px; color: #6b5a7a; font-size: 12px; } }
  @media (max-width: 520px) { .toast { right: 16px; bottom: 16px; } }
</style>
