<script lang="ts">
  import type { Snippet } from 'svelte';
  import { adminSections } from '../data/admin';

  let {
    title,
    active,
    navigate,
    children,
  }: {
    title: string;
    active: (typeof adminSections)[number];
    navigate: (path: string) => void;
    children: Snippet;
  } = $props();

  let menuOpen = $state(false);
  let notice = $state('');

  const routes: Partial<Record<(typeof adminSections)[number], string>> = {
    Resumen: '/admin/dashboard',
    Productos: '/admin/productos',
    Pedidos: '/admin/pedidos',
    Usuarios: '/admin/usuarios',
  };

  function openSection(section: (typeof adminSections)[number]) {
    const route = routes[section];
    if (route) navigate(route);
    else notice = `${section} estará disponible en su módulo asignado.`;
    menuOpen = false;
  }

  function logout() {
    sessionStorage.removeItem('dulcepack-admin-session');
    navigate('/admin');
  }
</script>

<div class="admin-shell">
  {#if menuOpen}<button class="scrim" aria-label="Cerrar menú al tocar fuera" onclick={() => menuOpen = false}></button>{/if}
  <aside class:open={menuOpen} class="sidebar">
    <div class="sidebar-top">
      <div class="sidebar-head">
        <a href="/admin/dashboard" onclick={(event) => { event.preventDefault(); navigate('/admin/dashboard'); }}>DulcePack</a>
        <p>Panel administrativo</p>
      </div>
      <button class="close-menu" type="button" aria-label="Cerrar menú administrativo" onclick={() => menuOpen = false}>×</button>
    </div>

    <nav aria-label="Administración">
      {#each adminSections as section}
        <button class:active={section === active} type="button" aria-current={section === active ? 'page' : undefined} onclick={() => openSection(section)}>{section}</button>
      {/each}
    </nav>
    <button class="logout" type="button" onclick={logout}>Cerrar sesión</button>
  </aside>

  <main class="admin-main">
    <header class="topbar">
      <div class="title-group">
        <button class="menu-button" type="button" aria-label="Abrir menú administrativo" aria-expanded={menuOpen} onclick={() => menuOpen = true}>
          <span></span><span></span><span></span>
        </button>
        <h1>{title}</h1>
      </div>
      <div class="admin-user">
        <button class="notification" type="button" aria-label="Notificaciones" onclick={() => notice = 'No hay notificaciones nuevas.'}>
          <span aria-hidden="true">🔔</span>
        </button>
        <span class="avatar" aria-hidden="true"></span>
        <strong>Isaac · Admin</strong>
      </div>
    </header>

    <div class="admin-content">
      {@render children()}
    </div>
  </main>
</div>

{#if notice}<div class="shell-toast" role="status">{notice}<button type="button" aria-label="Cerrar aviso" onclick={() => notice = ''}>×</button></div>{/if}

<style>
  .admin-shell { min-height: 100vh; padding: 32px; display: grid; grid-template-columns: 250px minmax(0, 1fr); gap: 32px; background: #fff9fc; text-rendering: geometricPrecision; }
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
    color: white;
    background: transparent;
    font-weight: 600;
    text-align: left;
    transition: background .16s ease, color .16s ease, transform .16s ease;
  }
  nav button:hover, .logout:hover { background: rgba(255,255,255,.12); transform: translateX(2px); }
  nav button.active { color: var(--ink); background: var(--yellow); }
  .logout { margin-top: auto; }
  .admin-main { min-width: 0; }
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
  h1 { margin: 0; color: var(--ink); font: 800 22px/1.2 'Nunito Variable', sans-serif; letter-spacing: -.02em; }
  .menu-button { display: none; }
  .admin-user { display: flex; align-items: center; gap: 12px; color: var(--ink); font-size: 14px; }
  .notification { width: 28px; height: 38px; display: grid; place-items: center; border: 0; color: #c58a00; background: transparent; }
  .notification span { font-size: 18px; line-height: 1; }
  .avatar { width: 38px; height: 38px; display: block; border-radius: 50%; background: var(--pink); }
  .admin-content { min-width: 0; }
  .shell-toast { position: fixed; right: 24px; bottom: 24px; z-index: 50; width: min(420px, calc(100% - 32px)); display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 15px 18px; border-radius: 14px; color: white; background: var(--ink); box-shadow: 0 15px 35px rgba(36,18,58,.22); }
  .shell-toast button { border: 0; color: white; background: transparent; font-size: 24px; line-height: 1; }
  .scrim { display: none; }

  @media (max-width: 1120px) {
    .admin-shell { grid-template-columns: 220px minmax(0, 1fr); gap: 22px; padding: 24px; }
    .sidebar { top: 24px; height: calc(100vh - 48px); }
  }
  @media (max-width: 800px) {
    .admin-shell { display: block; padding: 16px; }
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
    .sidebar-top { display: flex; justify-content: space-between; gap: 12px; }
    .close-menu { width: 40px; height: 40px; display: grid; place-items: center; border: 0; border-radius: 10px; color: white; background: rgba(255,255,255,.12); font-size: 28px; }
    .scrim { position: fixed; inset: 0; z-index: 20; display: block; border: 0; background: rgba(36,18,58,.42); backdrop-filter: blur(3px); }
    .menu-button { width: 42px; height: 42px; display: grid; place-content: center; gap: 4px; border: 0; border-radius: 10px; color: var(--purple); background: #f5edff; }
    .menu-button span { width: 19px; height: 2px; display: block; border-radius: 2px; background: currentColor; }
  }
  @media (max-width: 520px) {
    .topbar { padding: 12px 14px; }
    .admin-user { gap: 8px; }
    .admin-user strong { display: none; }
    h1 { font-size: 20px; }
    .shell-toast { right: 16px; bottom: 16px; }
  }
</style>
