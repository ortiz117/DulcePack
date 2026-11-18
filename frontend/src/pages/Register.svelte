<script lang="ts">
  let { navigate, register }: { navigate: (path: string) => void; register: (name: string) => void } = $props();
  let name = $state('');
  let email = $state('');
  let password = $state('');
  let confirmation = $state('');
  let notice = $state('');

  function submit(event: SubmitEvent) {
    event.preventDefault();
    if (password !== confirmation) {
      notice = 'Las contraseñas no coinciden. Revisa ambos campos.';
      return;
    }
    register(name);
  }
</script>

<main class="auth-page">
  <section class="auth-card">
    <aside class="brand-panel">
      <a href="/" onclick={(event) => { event.preventDefault(); navigate('/'); }}>DulcePack</a>
      <p>Tu próxima caja de antojos está a unos pasos.</p>
    </aside>
    <section class="form-panel" aria-labelledby="register-title">
      <form onsubmit={submit}>
        <h1 id="register-title">Crea tu cuenta</h1>
        <p class="intro">Regístrate para guardar pedidos y comprar más rápido.</p>
        <label>Nombre completo<input required autocomplete="name" bind:value={name} placeholder="Tu nombre" /></label>
        <label>Correo electrónico<input type="email" required autocomplete="email" bind:value={email} placeholder="nombre@correo.com" /></label>
        <label>Contraseña<input type="password" required minlength="8" autocomplete="new-password" bind:value={password} placeholder="Mínimo 8 caracteres" /></label>
        <label>Confirmar contraseña<input type="password" required minlength="8" autocomplete="new-password" bind:value={confirmation} placeholder="Repite tu contraseña" /></label>
        <button class="primary" type="submit">Crear cuenta</button>
        <button class="link" type="button" onclick={() => navigate('/iniciar-sesion')}>¿Ya tienes cuenta? Inicia sesión</button>
      </form>
    </section>
  </section>
</main>
{#if notice}<div class="notice" role="status">{notice}</div>{/if}

<style>
  .auth-page { min-height: 100vh; padding: 24px 5%; display: grid; place-items: start center; background: #fff9fc; }
  .auth-card { width: min(1296px, 100%); min-height: 960px; display: grid; grid-template-columns: .86fr 1.14fr; overflow: hidden; border-radius: 28px; background: white; }
  .brand-panel { padding: 72px 56px; background: var(--purple); color: white; }
  .brand-panel a { color: white; font: 600 54px 'Fredoka Variable', sans-serif; text-decoration: none; }
  .brand-panel p { margin-top: 22px; font-size: 18px; }
  .form-panel { padding: 72px 96px; }
  form { width: min(440px, 100%); }
  h1 { margin: 0 0 18px; font: 600 38px/1.1 'Fredoka Variable', sans-serif; }
  .intro { margin: 0 0 28px; color: var(--muted); font-size: 16px; }
  label { display: grid; gap: 8px; margin-bottom: 24px; color: var(--muted); font-size: 13px; }
  input { min-height: 56px; padding: 0 16px; border: 1px solid #dfd2e8; border-radius: 12px; color: var(--ink); background: white; }
  input:focus { border-color: var(--purple); }
  .primary { display: block; min-width: 180px; min-height: 48px; margin: 24px 0; border: 0; border-radius: 12px; background: var(--purple); color: white; font-weight: 800; }
  .link { display: block; border: 0; background: transparent; color: var(--purple); padding: 0; font-weight: 700; text-align: left; }
  .notice { position: fixed; left: 50%; bottom: 20px; transform: translateX(-50%); width: min(560px, calc(100% - 32px)); padding: 16px 20px; border-radius: 14px; background: var(--ink); color: white; }
  @media (max-width: 700px) {
    .auth-page { display: block; padding: 0; }
    .auth-card { min-height: 100vh; display: block; border-radius: 0; }
    .brand-panel { padding: 28px 24px; text-align: center; }
    .brand-panel a { font-size: 34px; }
    .brand-panel p { display: none; }
    .form-panel { padding: 38px 24px; }
    form { margin: 0 auto; }
    h1 { font-size: 32px; }
    label { margin-bottom: 18px; }
    .primary { width: 100%; }
  }
</style>
