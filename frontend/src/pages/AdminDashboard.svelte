<script lang="ts">
  import { adminMetrics, adminSections, weeklySales } from '../data/admin';

  let { navigate }: { navigate: (path: string) => void } = $props();
  let menuOpen = $state(false);
  let notice = $state('');
  const maxSale = Math.max(...weeklySales.map(sale => sale.value));
  const sectionPaths: Record<(typeof adminSections)[number], string> = {
    Resumen: '/admin/dashboard',
    Productos: '/admin/productos',
    Inventario: '/admin/inventario',
    Pedidos: '/admin/pedidos',
    Usuarios: '/admin/usuarios',
  };

  function navigateToSection(section: (typeof adminSections)[number]) {
    menuOpen = false;
    navigate(sectionPaths[section]);
  }

  function logout() {
    sessionStorage.removeItem('dulcepack-admin-session');
    navigate('/admin');
  }
</script>

<svelte:head><title>Resumen administrativo | DulcePack</title></svelte:head>

<div class="dashboard-shell">
  {#if menuOpen}<button class="scrim" aria-label="Cerrar menú al tocar fuera" onclick={() => menuOpen = false}></button>{/if}
  <aside class:open={menuOpen} class="sidebar">
    <div>
      <div class="sidebar-head">
        <a href="/admin/dashboard" onclick={(event) => { event.preventDefault(); navigate('/admin/dashboard'); }}>DulcePack</a>
        <p>Panel administrativo</p>
      </div>
      <button class="close-menu" type="button" aria-label="Cerrar menú administrativo" onclick={() => menuOpen = false}>×</button>
    </div>

    <nav aria-label="Administración">
      {#each adminSections as section, index}
        <button class:active={index === 0} type="button" aria-current={index === 0 ? 'page' : undefined} onclick={() => navigateToSection(section)}>{section}</button>
      {/each}
    </nav>
    <button class="logout" type="button" onclick={logout}>Cerrar sesión</button>
  </aside>

  <main class="dashboard-main">
    <header class="topbar">
      <div class="title-group">
        <button class="menu-button" type="button" aria-label="Abrir menú administrativo" aria-expanded={menuOpen} onclick={() => menuOpen = true}>
          <span></span><span></span><span></span>
        </button>
        <h1>Resumen</h1>
      </div>
      <div class="admin-user">
        <button class="notification" type="button" aria-label="Notificaciones" onclick={() => notice = 'No hay notificaciones nuevas.'}>
          <span aria-hidden="true">🔔</span>
        </button>
        <span class="avatar" aria-hidden="true"></span>
        <strong>Isaac · Admin</strong>
      </div>
    </header>

    <section class="metrics" aria-label="Indicadores principales">
      {#each adminMetrics as metric}
        <article class="metric-card {metric.tone}">
          <p>{metric.label}</p>
          <strong>{metric.value}</strong>
          <span>{metric.helper}</span>
        </article>
      {/each}
    </section>

    <section class="dashboard-grid">
      <article class="chart-card">
        <div class="card-heading">
          <h2>Ventas semanales</h2>
        </div>
        <div class="chart" role="img" aria-label="Gráfica de ventas semanales">
          {#each weeklySales as sale}
            <div class="bar-column" title={`${sale.label}: índice ${sale.value}`}>
              <div class="bar-track"><span class="bar" style={`height: ${(sale.value / maxSale) * 100}%`}></span></div>
              <span class="day" aria-hidden="true">{sale.day}</span>
            </div>
          {/each}
        </div>
      </article>

      <aside class="quick-actions" aria-labelledby="actions-title">
        <h2 id="actions-title">Acciones rápidas</h2>
        <div class="action-buttons">
          <button class="primary-action" type="button" onclick={() => navigateToSection('Productos')}>Gestionar productos</button>
          <button type="button" onclick={() => navigateToSection('Pedidos')}>Revisar pedidos</button>
          <button type="button" onclick={() => navigateToSection('Inventario')}>Actualizar inventario</button>
        </div>
      </aside>
    </section>
  </main>
</div>

{#if notice}<div class="toast" role="status">{notice}<button type="button" aria-label="Cerrar aviso" onclick={() => notice = ''}>×</button></div>{/if}

<style>
  .dashboard-shell { min-height: 100vh; padding: 32px; display: grid; grid-template-columns: 250px minmax(0, 1fr); gap: 32px; background: #fff9fc; text-rendering: geometricPrecision; }
  .sidebar {
    position: sticky;
    top: 32px;
    height: calc(100vh - 64px);
    min-height: 680px;
    max-height: 760px;
    display: flex;
    flex-direction: column;
    padding: 28px 20px 26px;
    border-radius: 20px;
    color: white;
    background: var(--purple);
    box-shadow: 0 18px 42px rgba(109,40,217,.2);
  }
  .sidebar-head { padding: 0 0 28px; }
  .sidebar-head a { color: white; font: 700 30px/1 'Fredoka Variable', sans-serif; letter-spacing: -.03em; text-decoration: none; }
  .sidebar-head p { margin: 7px 0 0; font-size: 13px; }
  .close-menu { display: none; }
  nav { display: grid; gap: 10px; }
  nav button, .logout {
    min-height: 48px;
    padding: 0 14px;
    border: 0;
    border-radius: 12px;
    background: transparent;
    color: white;
    font-weight: 800;
    text-align: left;
    transition: background .16s ease, color .16s ease, transform .16s ease;
  }
  nav button:hover, .logout:hover { background: rgba(255,255,255,.12); transform: translateX(2px); }
  nav button.active { color: var(--ink); background: var(--yellow); }
  .logout { margin-top: auto; }
  .dashboard-main { min-width: 0; }
  .topbar {
    min-height: 72px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24px;
    padding: 14px 24px;
    border-radius: 16px;
    background: white;
    box-shadow: 0 8px 24px rgba(38,22,50,.08);
  }
  .title-group { display: flex; align-items: center; gap: 14px; }
  h1, h2 { margin: 0; color: var(--ink); font-family: 'Nunito Variable', sans-serif; font-weight: 800; letter-spacing: -.02em; }
  h1 { font-size: 22px; }
  h2 { font-size: 23px; }
  .menu-button { display: none; }
  .admin-user { display: flex; align-items: center; gap: 12px; color: var(--ink); font-size: 14px; }
  .notification { width: 28px; height: 38px; display: grid; place-items: center; border: 0; background: transparent; color: #c58a00; }
  .notification span { font-size: 18px; line-height: 1; }
  .avatar { width: 38px; height: 38px; display: block; border-radius: 50%; background: var(--pink); }
  .metrics { margin-top: 24px; display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 20px; }
  .metric-card {
    min-height: 150px;
    padding: 22px;
    border-radius: 17px;
    background: white;
    box-shadow: 0 5px 20px rgba(38,22,50,.06);
  }
  .metric-card p { margin: 0; color: #776a83; font-size: 14px; }
  .metric-card strong { display: block; margin-top: 12px; color: currentColor; font: 800 30px/1.1 'Fredoka Variable', sans-serif; }
  .metric-card span { display: block; margin-top: 12px; color: #5f526b; font-size: 13px; font-weight: 700; }
  .metric-card.purple { color: var(--purple); }
  .metric-card.pink { color: var(--pink); }
  .metric-card.yellow { color: #a86f00; }
  .metric-card.yellow strong { color: #a86f00; background: var(--yellow); background-clip: text; -webkit-background-clip: text; -webkit-text-fill-color: transparent; filter: drop-shadow(0 .5px 0 #8f5f00); }
  .dashboard-grid { margin-top: 24px; display: grid; grid-template-columns: minmax(0, 1.8fr) minmax(300px, 1fr); gap: 20px; }
  .chart-card { min-height: 429px; padding: 22px 22px 18px; border-radius: 17px; background: white; box-shadow: 0 5px 20px rgba(38,22,50,.06); }
  .card-heading { display: block; }
  .chart { height: 315px; display: grid; grid-template-columns: repeat(7, 44px); justify-content: space-between; align-items: end; gap: 0; padding: 18px 40px 0; }
  .bar-column { height: 100%; display: grid; grid-template-rows: 1fr 24px; align-items: end; justify-items: center; }
  .bar-track { width: min(44px, 100%); height: 100%; display: flex; align-items: end; }
  .bar { width: 100%; min-height: 12px; border-radius: 11px; background: var(--purple); }
  .day { color: #665a70; font-size: 12px; }
  .quick-actions { min-height: 429px; display: flex; flex-direction: column; gap: 10px; padding: 24px 20px; border-radius: 17px; background: #fff3c4; }
  .action-buttons { display: grid; gap: 10px; align-content: start; }
  .action-buttons button { width: min(190px, 100%); min-height: 48px; padding: 0 10px; border: 2px solid var(--purple); border-radius: 12px; color: var(--purple); background: white; font-size: 13px; font-weight: 800; white-space: nowrap; }
  .action-buttons button:hover { transform: translateY(-1px); box-shadow: 0 8px 16px rgba(64,37,79,.1); }
  .action-buttons .primary-action { border-color: var(--purple); color: white; background: var(--purple); }
  .toast { position: fixed; right: 24px; bottom: 24px; z-index: 40; width: min(420px, calc(100% - 32px)); display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 15px 18px; border-radius: 14px; color: white; background: var(--ink); box-shadow: 0 15px 35px rgba(36,18,58,.22); }
  .toast button { border: 0; color: white; background: transparent; font-size: 24px; line-height: 1; }
  .scrim { display: none; }

  @media (max-width: 1120px) {
    .dashboard-shell { grid-template-columns: 220px minmax(0, 1fr); gap: 22px; padding: 24px; }
    .sidebar { top: 24px; height: calc(100vh - 48px); }
    .metrics { grid-template-columns: repeat(2, minmax(0, 1fr)); }
    .metric-card:last-child { grid-column: 1 / -1; }
    .dashboard-grid { grid-template-columns: 1fr; }
    .quick-actions { min-height: 250px; }
    .action-buttons { grid-template-columns: repeat(3, minmax(0, 1fr)); }
    .action-buttons button { width: 100%; }
  }
  @media (max-width: 800px) {
    .dashboard-shell { display: block; padding: 16px; }
    .sidebar {
      position: fixed;
      z-index: 30;
      inset: 12px auto 12px 12px;
      width: min(280px, calc(100vw - 48px));
      height: auto;
      min-height: 0;
      max-height: none;
      transform: translateX(calc(-100% - 24px));
      visibility: hidden;
      transition: transform .22s ease, visibility 0s linear .22s;
    }
    .sidebar.open { transform: translateX(0); visibility: visible; transition-delay: 0s; }
    .sidebar > div:first-child { display: flex; justify-content: space-between; gap: 12px; }
    .close-menu { width: 40px; height: 40px; display: grid; place-items: center; border: 0; border-radius: 10px; color: white; background: rgba(255,255,255,.12); font-size: 28px; }
    .scrim { position: fixed; inset: 0; z-index: 20; display: block; border: 0; background: rgba(36,18,58,.42); backdrop-filter: blur(3px); }
    .menu-button { width: 42px; height: 42px; display: grid; place-content: center; gap: 4px; border: 0; border-radius: 10px; color: var(--purple); background: #f5edff; }
    .menu-button span { width: 19px; height: 2px; display: block; border-radius: 2px; background: currentColor; }
    .metrics { grid-template-columns: 1fr; }
    .metric-card:last-child { grid-column: auto; }
    .chart { height: 270px; grid-template-columns: repeat(7, minmax(24px, 34px)); padding-inline: 0; gap: 8px; }
    .bar-track { width: 100%; }
    .action-buttons { grid-template-columns: 1fr; }
  }
  @media (max-width: 520px) {
    .topbar { padding: 12px 14px; }
    .admin-user { gap: 8px; }
    .admin-user strong { display: none; }
    h1 { font-size: 20px; }
    h2 { font-size: 21px; }
    .metrics, .dashboard-grid { margin-top: 16px; gap: 16px; }
    .metric-card { min-height: 138px; }
    .chart-card { min-height: 390px; padding: 20px 15px 14px; }
    .chart { height: 275px; }
    .quick-actions { min-height: 330px; }
    .toast { right: 16px; bottom: 16px; }
  }
</style>
