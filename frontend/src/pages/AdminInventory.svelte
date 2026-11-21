<script lang="ts">
  import AdminShell from '../components/AdminShell.svelte';
  import { inventoryItems, inventoryMetrics } from '../data/admin';

  let { navigate }: { navigate: (path: string) => void } = $props();
</script>

<svelte:head><title>Inventario administrativo | DulcePack</title></svelte:head>

<AdminShell title="Inventario" active="Inventario" {navigate}>
  <section class="inventory-metrics" aria-label="Resumen de inventario">
    {#each inventoryMetrics as metric}
      <article class="metric-card {metric.tone}">
        <p>{metric.label}</p>
        <strong>{metric.value}</strong>
      </article>
    {/each}
  </section>

  <section class="inventory-panel" aria-label="Existencias por producto">
    <p class="scroll-hint">Desliza horizontalmente para ver todos los datos.</p>
    <!-- svelte-ignore a11y_no_noninteractive_tabindex (región desplazable accesible por teclado) -->
    <div class="table-scroll" role="region" aria-label="Tabla desplazable de inventario" tabindex="0">
      <table aria-label="Inventario de productos">
        <thead>
          <tr>
            <th scope="col">Producto</th>
            <th scope="col">Disponible</th>
            <th scope="col">Mínimo</th>
            <th scope="col">Estado</th>
            <th scope="col">Actualización</th>
          </tr>
        </thead>
        <tbody>
          {#each inventoryItems as item (item.product)}
            <tr>
              <th scope="row">{item.product}</th>
              <td>{item.available}</td>
              <td>{item.minimum}</td>
              <td><span class:low={item.status === 'Bajo'} class="status-badge">{item.status}</span></td>
              <td>{item.updatedAt}</td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  </section>
</AdminShell>

<style>
  .inventory-metrics { margin-top: 24px; display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 20px; }
  .metric-card { min-height: 140px; padding: 22px 20px; border-radius: 17px; background: white; box-shadow: 0 5px 20px rgba(38,22,50,.06); }
  .metric-card p { margin: 0; color: #776a83; font-size: 14px; }
  .metric-card strong { display: block; margin-top: 12px; font: 800 30px/1.1 'Fredoka Variable', sans-serif; }
  .metric-card.purple strong { color: var(--purple); }
  .metric-card.pink strong { color: #e92532; }
  .metric-card.ink strong { color: #5c496d; }
  .inventory-panel { margin-top: 24px; min-height: 650px; padding: 20px; overflow: hidden; border-radius: 18px; background: white; box-shadow: 0 5px 20px rgba(38,22,50,.06); }
  .table-scroll { width: 100%; overflow-x: auto; border-radius: 12px; scrollbar-gutter: stable; -webkit-overflow-scrolling: touch; }
  .table-scroll::-webkit-scrollbar { height: 10px; }
  .table-scroll::-webkit-scrollbar-thumb { border: 2px solid white; border-radius: 999px; background: #cdb9e8; }
  .scroll-hint { display: none; }
  table { width: 100%; min-width: 850px; border-collapse: collapse; table-layout: fixed; color: #6b5a7a; font-size: 14px; }
  th, td { height: 66px; padding: 0 16px; border-bottom: 1px solid #e8def2; text-align: left; font-weight: 500; }
  thead th { height: 48px; border: 0; color: #6b5a7a; background: #f7f3ff; font-size: 12px; font-weight: 700; }
  thead th:first-child { border-radius: 12px 0 0 12px; }
  thead th:last-child { border-radius: 0 12px 12px 0; }
  thead th:nth-child(1), tbody th { width: 29%; }
  thead th:nth-child(2), thead th:nth-child(3) { width: 15%; }
  thead th:nth-child(4) { width: 19%; }
  thead th:nth-child(5) { width: 22%; }
  tbody th { color: var(--ink); font-weight: 600; }
  .status-badge { width: 100%; max-width: 188px; min-height: 30px; display: inline-grid; place-items: center; border-radius: 999px; color: var(--ink); background: #f7f3ff; font-size: 12px; font-weight: 700; }
  .status-badge.low { color: #c51432; background: #fce3f0; }

  @media (max-width: 800px) {
    .inventory-metrics { grid-template-columns: repeat(2, minmax(0, 1fr)); }
    .metric-card:last-child { grid-column: 1 / -1; }
    .inventory-panel { padding: 14px; min-height: 560px; }
    .scroll-hint { display: block; margin: 0 0 10px; color: #6b5a7a; font-size: 12px; }
    .table-scroll { padding-bottom: 4px; }
  }

  @media (max-width: 520px) {
    .inventory-metrics { margin-top: 16px; grid-template-columns: 1fr; gap: 16px; }
    .metric-card:last-child { grid-column: auto; }
    .metric-card { min-height: 126px; }
    .inventory-panel { margin-top: 16px; }
  }
</style>