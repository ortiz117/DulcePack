<script lang="ts">
  import AdminShell from '../components/AdminShell.svelte';
  import { adminUsers } from '../data/admin';

  let { navigate }: { navigate: (path: string) => void } = $props();
</script>

<svelte:head><title>Usuarios administrativos | DulcePack</title></svelte:head>

<AdminShell title="Usuarios" active="Usuarios" {navigate}>
  <section class="table-panel" aria-label="Listado de usuarios">
    <p class="scroll-hint">Desliza horizontalmente para ver todos los datos.</p>
    <!-- svelte-ignore a11y_no_noninteractive_tabindex (región desplazable accesible por teclado) -->
    <div class="table-scroll" role="region" aria-label="Tabla desplazable de usuarios" tabindex="0">
      <table aria-label="Usuarios administrados">
        <thead><tr><th scope="col">Nombre</th><th scope="col">Correo</th><th scope="col">Rol</th><th scope="col">Pedidos</th><th scope="col">Estado</th></tr></thead>
        <tbody>
          {#each adminUsers as user (user.email)}
            <tr>
              <th scope="row">{user.name}</th><td>{user.email}</td><td>{user.role}</td><td>{user.orders ?? '—'}</td>
              <td><span class:suspended={user.status === 'Suspendido'} class="status-badge">{user.status}</span></td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  </section>
</AdminShell>

<style>
  .table-panel { margin-top: 35px; min-height: 760px; padding: 20px; overflow: hidden; border-radius: 18px; background: white; box-shadow: 0 5px 20px rgba(38,22,50,.06); }
  .table-scroll { width: 100%; overflow-x: auto; border-radius: 12px; scrollbar-gutter: stable; -webkit-overflow-scrolling: touch; }
  .scroll-hint { display: none; }
  table { width: 100%; min-width: 800px; border-collapse: collapse; table-layout: fixed; color: #6b5a7a; font-size: 14px; }
  th, td { height: 66px; padding: 0 16px; border-bottom: 1px solid #e8def2; text-align: left; font-weight: 500; }
  thead th { height: 48px; border: 0; color: #6b5a7a; background: #f7f3ff; font-size: 12px; font-weight: 700; }
  thead th:first-child { border-radius: 12px 0 0 12px; } thead th:last-child { border-radius: 0 12px 12px 0; }
  thead th:nth-child(1) { width: 22%; } thead th:nth-child(2) { width: 29%; } thead th:nth-child(3) { width: 20%; }
  tbody th { color: var(--ink); font-weight: 700; }
  .status-badge { min-width: 96px; min-height: 30px; padding: 0 12px; display: inline-grid; place-items: center; border-radius: 999px; color: #54258a; background: #eee4ff; font-size: 12px; font-weight: 700; }
  .status-badge.suspended { color: #a51f5d; background: #fce7f3; }
  @media (max-width: 800px) { .table-panel { margin-top: 20px; min-height: 620px; padding: 14px; } .scroll-hint { display: block; margin: 0 0 10px; color: #6b5a7a; font-size: 12px; } }
</style>
