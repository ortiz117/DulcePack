<script lang="ts">
  import AdminShell from '../components/AdminShell.svelte';
  import { adminProducts } from '../data/admin';

  let { navigate }: { navigate: (path: string) => void } = $props();
  let query = $state('');
  let notice = $state('');
  let filteredProducts = $derived(adminProducts.filter(product => product.name.toLocaleLowerCase('es-MX').includes(query.trim().toLocaleLowerCase('es-MX'))));

  function announce(message: string) {
    notice = message;
  }
</script>

<svelte:head><title>Productos administrativos | DulcePack</title></svelte:head>

<AdminShell title="Productos" active="Resumen" {navigate}>
  <section class="product-actions" aria-label="Herramientas de productos">
    <label class="search-field">
      <span class="sr-only">Buscar producto</span>
      <input type="search" aria-label="Buscar producto" placeholder="Buscar producto..." bind:value={query} />
    </label>
    <button class="new-product" type="button" onclick={() => announce('Nuevo producto se implementará en su vista asignada.')}>Nuevo producto</button>
  </section>

  <section class="products-panel" aria-label="Listado de productos">
    <p class="scroll-hint">Desliza horizontalmente para ver todos los datos.</p>
    <!-- svelte-ignore a11y_no_noninteractive_tabindex (región desplazable accesible por teclado) -->
    <div class="table-scroll" role="region" aria-label="Tabla desplazable de productos" tabindex="0">
      <table aria-label="Productos administrados">
        <thead>
          <tr>
            <th scope="col">Producto</th>
            <th scope="col">Categoría</th>
            <th scope="col">Precio</th>
            <th scope="col">Stock</th>
            <th scope="col">Estado</th>
            <th scope="col">Acciones</th>
          </tr>
        </thead>
        <tbody>
          {#each filteredProducts as product (product.id)}
            <tr>
              <th scope="row">{product.name}</th>
              <td>{product.category}</td>
              <td>${product.price}</td>
              <td>{product.stock}</td>
              <td><span class:low={product.status === 'Bajo'} class="status-badge">{product.status}</span></td>
              <td><button class="edit-action" type="button" aria-label={`Editar ${product.name}`} onclick={() => announce(`La edición de ${product.name} estará disponible en su vista asignada.`)}>Editar</button></td>
            </tr>
          {:else}
            <tr class="empty-row"><td colspan="6">No encontramos productos con ese nombre.</td></tr>
          {/each}
        </tbody>
      </table>
    </div>
  </section>
</AdminShell>

{#if notice}<div class="toast" role="status">{notice}<button type="button" aria-label="Cerrar aviso" onclick={() => notice = ''}>×</button></div>{/if}

<style>
  .product-actions { margin-top: 35px; display: flex; align-items: center; justify-content: space-between; gap: 24px; }
  .search-field { width: min(420px, 100%); }
  .search-field input { width: 100%; min-height: 48px; padding: 0 16px; border: 1px solid #e8def2; border-radius: 12px; color: var(--ink); background: white; font-size: 14px; }
  .search-field input::placeholder { color: #6b5a7a; opacity: 1; }
  .search-field input:focus { border-color: var(--purple); outline: 0; box-shadow: 0 0 0 4px rgba(109,40,217,.12); }
  .sr-only { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden; clip: rect(0,0,0,0); white-space: nowrap; border: 0; }
  .new-product { width: 180px; min-height: 48px; border: 0; border-radius: 12px; color: white; background: var(--purple); font-size: 14px; font-weight: 800; }
  .new-product:hover { background: #5d1fc3; }
  .products-panel { margin-top: 35px; min-height: 760px; padding: 20px; overflow: hidden; border-radius: 18px; background: white; box-shadow: 0 5px 20px rgba(38,22,50,.06); }
  .table-scroll { width: 100%; overflow-x: auto; border-radius: 12px; scrollbar-gutter: stable; -webkit-overflow-scrolling: touch; }
  .table-scroll::-webkit-scrollbar { height: 10px; }
  .table-scroll::-webkit-scrollbar-thumb { border: 2px solid white; border-radius: 999px; background: #cdb9e8; }
  .scroll-hint { display: none; }
  table { width: 100%; min-width: 900px; border-collapse: collapse; table-layout: fixed; color: #6b5a7a; font-size: 14px; }
  th, td { height: 66px; padding: 0 16px; border-bottom: 1px solid #e8def2; text-align: left; font-weight: 500; }
  thead th { height: 48px; border: 0; color: #6b5a7a; background: #f7f3ff; font-size: 12px; font-weight: 700; }
  thead th:first-child { border-radius: 12px 0 0 12px; }
  thead th:last-child { border-radius: 0 12px 12px 0; }
  thead th:nth-child(1), tbody th { width: 25%; }
  thead th:nth-child(2) { width: 14%; }
  thead th:nth-child(3), thead th:nth-child(4) { width: 13%; }
  thead th:nth-child(5) { width: 16%; }
  thead th:nth-child(6) { width: 19%; }
  tbody th { color: var(--ink); font-weight: 600; }
  .status-badge { width: 148px; min-height: 30px; display: inline-grid; place-items: center; border-radius: 999px; color: var(--ink); background: #f7f3ff; font-size: 12px; font-weight: 700; }
  .status-badge.low { color: #b51f61; background: #fce7f3; }
  .edit-action { min-height: 44px; padding: 0; border: 0; color: #6b5a7a; background: transparent; font-weight: 600; text-align: left; }
  .edit-action:hover { color: var(--purple); text-decoration: underline; }
  .empty-row td { height: 120px; color: #6b5a7a; text-align: center; }
  .toast { position: fixed; right: 24px; bottom: 24px; z-index: 50; width: min(460px, calc(100% - 32px)); display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 15px 18px; border-radius: 14px; color: white; background: var(--ink); box-shadow: 0 15px 35px rgba(36,18,58,.22); }
  .toast button { border: 0; color: white; background: transparent; font-size: 24px; line-height: 1; }

  @media (max-width: 800px) {
    .product-actions { margin-top: 24px; align-items: stretch; flex-direction: column; gap: 12px; }
    .search-field, .new-product { width: 100%; }
    .products-panel { margin-top: 20px; min-height: 620px; padding: 14px; }
    .scroll-hint { display: block; margin: 0 0 10px; color: #6b5a7a; font-size: 12px; }
    .table-scroll { padding-bottom: 4px; }
  }
  @media (max-width: 520px) {
    .toast { right: 16px; bottom: 16px; }
  }
</style>
