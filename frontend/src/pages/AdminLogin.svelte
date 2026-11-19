<script lang="ts">
  let { navigate }: { navigate: (path: string) => void } = $props();

  const demoEmail = 'admin@dulcepack.mx';
  const demoPassword = 'DulcePack2026';
  let email = $state(demoEmail);
  let password = $state(demoPassword);
  let error = $state('');
  let submitting = $state(false);

  function submit(event: SubmitEvent) {
    event.preventDefault();
    error = '';
    submitting = true;

    if (email.trim().toLowerCase() !== demoEmail || password !== demoPassword) {
      error = 'Credenciales incorrectas. Usa el acceso de demostración mostrado en el formulario.';
      submitting = false;
      return;
    }

    sessionStorage.setItem('dulcepack-admin-session', 'active');
    navigate('/admin/dashboard');
  }
</script>

<svelte:head><title>Acceso administrativo | DulcePack</title></svelte:head>

<main class="admin-login">
  <section class="login-card" aria-labelledby="admin-login-title">
    <aside class="brand-panel">
      <a href="/" onclick={(event) => { event.preventDefault(); navigate('/'); }}>DulcePack</a>
      <p>Controla productos, inventario y pedidos<br />desde un solo lugar.</p>
    </aside>

    <div class="form-panel">
      <form onsubmit={submit}>
        <h1 id="admin-login-title">Acceso administrativo</h1>
        <p class="intro">Ingresa tus credenciales para continuar.</p>

        <label for="admin-email">Correo</label>
        <input id="admin-email" type="email" required autocomplete="username" bind:value={email} aria-invalid={error ? 'true' : 'false'} />

        <label for="admin-password">Contraseña</label>
        <input id="admin-password" type="password" required autocomplete="current-password" bind:value={password} aria-invalid={error ? 'true' : 'false'} />

        {#if error}<p class="error" role="alert">{error}</p>{/if}
        <button class="submit" type="submit" disabled={submitting}>{submitting ? 'Entrando…' : 'Entrar al panel'}</button>
      </form>
    </div>
  </section>
</main>

<style>
  .admin-login {
    min-height: 100vh;
    display: grid;
    place-items: center;
    padding: 40px 24px;
    background: #fff9fc;
    text-rendering: geometricPrecision;
  }
  .login-card {
    width: min(1100px, 100%);
    min-height: 680px;
    display: grid;
    grid-template-columns: 480px minmax(0, 1fr);
    overflow: hidden;
    border-radius: 26px;
    background: white;
    box-shadow: 0 22px 50px rgba(45, 20, 65, .12);
  }
  .brand-panel {
    position: relative;
    overflow: hidden;
    padding: 86px 56px;
    color: white;
    background: var(--purple);
  }
  .brand-panel a {
    position: relative;
    z-index: 1;
    color: white;
    font: 700 58px/1 'Fredoka Variable', sans-serif;
    letter-spacing: -.035em;
    text-decoration: none;
  }
  .brand-panel p {
    position: relative;
    z-index: 1;
    margin: 28px 0 0;
    font-size: 18px;
    line-height: 1.55;
  }
  .form-panel {
    display: flex;
    align-items: flex-start;
    padding: 88px 80px;
  }
  form { width: min(360px, 100%); }
  h1 {
    margin: 0;
    color: var(--ink);
    font: 800 36px/1.15 'Nunito Variable', sans-serif;
    letter-spacing: -.025em;
  }
  .intro { margin: 18px 0 26px; color: #78688a; font-size: 16px; line-height: 1.45; }
  label { display: block; margin: 0 0 8px; color: #655575; font-size: 13px; font-weight: 700; }
  input {
    width: 100%;
    min-height: 56px;
    margin-bottom: 26px;
    padding: 0 16px;
    border: 1px solid #e3d6ed;
    border-radius: 11px;
    background: white;
    color: #776986;
    font-size: 16px;
    transition: border-color .18s ease, box-shadow .18s ease;
  }
  input:hover { border-color: #cdb5e4; }
  input:focus { border-color: var(--purple); outline: 0; box-shadow: 0 0 0 4px rgba(109,40,217,.12); }
  input[aria-invalid='true'] { border-color: #c6284d; }
  .error { margin: -10px 0 18px; color: #a51d40; font-size: 13px; line-height: 1.4; }
  .submit {
    min-width: 180px;
    min-height: 50px;
    padding: 0 24px;
    border: 0;
    border-radius: 11px;
    background: var(--purple);
    color: white;
    font-weight: 800;
    transition: transform .18s ease, background .18s ease;
  }
  .submit:hover:not(:disabled) { background: #5d1fc3; transform: translateY(-2px); }
  .submit:active:not(:disabled) { transform: translateY(0); }
  .submit:disabled { opacity: .65; cursor: wait; }

  @media (max-width: 900px) {
    .login-card { grid-template-columns: 40% 60%; }
    .brand-panel { padding: 72px 38px; }
    .brand-panel a { font-size: 45px; }
    .form-panel { padding: 72px 48px; }
  }
  @media (max-width: 700px) {
    .admin-login { display: block; padding: 20px 16px; }
    .login-card { min-height: 0; display: block; border-radius: 21px; }
    .brand-panel { padding: 34px 24px 36px; }
    .brand-panel a { font-size: 42px; }
    .brand-panel p { margin-top: 16px; font-size: 16px; }
    .form-panel { padding: 38px 24px 42px; }
    form { margin: 0 auto; }
    h1 { font-size: 30px; }
    .submit { width: 100%; }
  }
</style>
