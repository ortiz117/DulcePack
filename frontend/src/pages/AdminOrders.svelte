<script lang="ts">
  import AdminShell from '../components/AdminShell.svelte';
  import { adminOrders } from '../data/admin';
  import type { AdminOrder } from '../data/admin';

  let { navigate }: { navigate: (path: string) => void } = $props();
  let selectedOrder = $state<AdminOrder | null>(null);
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
              <td><button type="button" aria-label={`Ver detalle de ${order.id}`} onclick={() => selectedOrder = order}>Ver detalle</button></td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  </section>
</AdminShell>

{#if selectedOrder}
  <div class="modal-backdrop" role="presentation" onclick={(event) => { if (event.currentTarget === event.target) selectedOrder = null; }}>
    <div class="order-detail" role="dialog" aria-modal="true" aria-labelledby="order-detail-title">
      <div class="detail-heading">
        <div><span>Pedido</span><h2 id="order-detail-title">Detalle del pedido {selectedOrder.id}</h2></div>
        <button class="close-detail" type="button" aria-label="Cerrar detalle" onclick={() => selectedOrder = null}>×</button>
      </div>
      <div class="detail-meta">
        <div><span>Cliente</span><strong>{selectedOrder.customer}</strong><small>{selectedOrder.email}</small></div>
        <div><span>Entrega</span><strong>{selectedOrder.date}</strong><small>{selectedOrder.address}</small></div>
        <div><span>Estado</span><strong>{selectedOrder.status}</strong></div>
      </div>
      <div class="item-list" aria-label="Productos del pedido">
        {#each selectedOrder.items as item}
          <div class="order-item"><div><strong>{item.product}</strong><small>Cantidad: {item.quantity}</small></div><span>${item.unitPrice * item.quantity}</span></div>
        {/each}
      </div>
      <div class="detail-totals">
        <span>Envío <strong>${selectedOrder.shipping}</strong></span>
        <span class="grand-total">Total <strong>${selectedOrder.total}</strong></span>
      </div>
    </div>
  </div>
{/if}

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
  .modal-backdrop { position: fixed; inset: 0; z-index: 80; display: grid; place-items: center; padding: 24px; background: rgba(35, 18, 50, .55); }
  .order-detail { width: min(680px, 100%); max-height: calc(100vh - 48px); overflow-y: auto; padding: 30px; border-radius: 22px; background: white; box-shadow: 0 24px 70px rgba(30, 12, 45, .28); }
  .detail-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; }
  .detail-heading span, .detail-meta span { display: block; color: var(--purple); font-size: 12px; font-weight: 800; text-transform: uppercase; letter-spacing: .04em; }
  .detail-heading h2 { margin: 5px 0 0; font: 700 28px/1.2 'Fredoka Variable', sans-serif; }
  .close-detail { width: 44px; height: 44px; flex: 0 0 auto; border: 0; border-radius: 50%; color: var(--ink); background: var(--lavender); font-size: 25px; }
  .detail-meta { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 18px; margin: 26px 0; padding: 20px; border-radius: 16px; background: #faf7fd; }
  .detail-meta strong, .detail-meta small { display: block; margin-top: 5px; }
  .detail-meta small { color: #756780; line-height: 1.4; }
  .item-list { display: grid; gap: 10px; }
  .order-item { display: flex; align-items: center; justify-content: space-between; gap: 20px; padding: 15px 0; border-bottom: 1px solid #eadff2; }
  .order-item strong, .order-item small { display: block; }
  .order-item small { margin-top: 4px; color: #756780; }
  .order-item > span { font-weight: 800; }
  .detail-totals { display: grid; justify-content: end; gap: 9px; margin-top: 22px; text-align: right; }
  .detail-totals span { display: flex; justify-content: space-between; gap: 40px; }
  .grand-total { color: var(--purple); font-size: 20px; }
  @media (max-width: 800px) { .table-panel { margin-top: 20px; min-height: 620px; padding: 14px; } .scroll-hint { display: block; margin: 0 0 10px; color: #6b5a7a; font-size: 12px; } }
  @media (max-width: 600px) { .modal-backdrop { align-items: end; padding: 0; } .order-detail { max-height: 88vh; padding: 24px 20px; border-radius: 24px 24px 0 0; } .detail-heading h2 { font-size: 23px; } .detail-meta { grid-template-columns: 1fr; } }
</style>
