<script lang="ts">
  import AdminShell from '../components/AdminShell.svelte';

  let { navigate }: { navigate: (path: string) => void } = $props();
  let status = $state('');
  let fileName = $state('');
  let imageError = $state('');
  let isDragging = $state(false);

  function saveProduct(event: SubmitEvent) {
    event.preventDefault();
    status = 'Producto guardado en modo demostración. Los cambios no son persistentes.';
  }

  function clearStatus() {
    status = '';
  }

  function selectImage(event: Event) {
    status = '';
    const file = (event.currentTarget as HTMLInputElement).files?.[0];
    if (file && !file.type.startsWith('image/')) {
      fileName = '';
      imageError = 'Selecciona un archivo de imagen válido.';
      return;
    }

    fileName = file?.name ?? '';
    imageError = '';
  }

  function startDragging(event: DragEvent) {
    event.preventDefault();
    isDragging = true;
  }

  function continueDragging(event: DragEvent) {
    event.preventDefault();
    if (event.dataTransfer) event.dataTransfer.dropEffect = 'copy';
  }

  function stopDragging(event: DragEvent) {
    event.preventDefault();
    isDragging = false;
  }

  function dropImage(event: DragEvent) {
    event.preventDefault();
    isDragging = false;
    status = '';
    const file = event.dataTransfer?.files[0];
    if (file && !file.type.startsWith('image/')) {
      fileName = '';
      imageError = 'Selecciona un archivo de imagen válido.';
      return;
    }

    fileName = file?.name ?? '';
    imageError = '';
  }
</script>

<svelte:head><title>Nuevo producto | DulcePack</title></svelte:head>

<AdminShell title="Nuevo producto" active="Productos" {navigate}>
  <div class="product-layout">
    <section class="product-form-card" aria-labelledby="product-information-title">
      <h2 id="product-information-title">Información del producto</h2>
      <form onsubmit={saveProduct} oninput={clearStatus}>
        <label>Nombre<input name="name" value="Pack Fiesta Dulce" required /></label>
        <label>Descripción<textarea name="description" required>Dulces variados para compartir</textarea></label>
        <div class="field-row">
          <label>Precio<input name="price" type="number" value="189" min="0.01" step="0.01" required /></label>
          <label>Stock<input name="stock" type="number" value="24" min="0" step="1" required /></label>
        </div>
        <label>Categoría
          <select name="category" required>
            <option>Paquetes</option>
            <option>Dulces</option>
            <option>Botanas</option>
          </select>
        </label>
        <label>Etiquetas<input name="tags" value="Fiesta, Compartir, Más vendido" required /></label>
        <button type="submit">Guardar producto</button>
      </form>
      {#if status}<p class="save-status" role="status">{status}</p>{/if}
    </section>

    <aside class="image-card" aria-labelledby="product-image-title">
      <h2 id="product-image-title">Imagen del producto</h2>
      <label
        class="upload-zone"
        class:is-dragging={isDragging}
        ondragenter={startDragging}
        ondragover={continueDragging}
        ondragleave={stopDragging}
        ondrop={dropImage}
      >
        <span>Arrastra una imagen o haz clic para subir</span>
        <input type="file" accept="image/*" aria-label="Imagen del producto" onchange={selectImage} />
      </label>
      {#if fileName}<p class="file-name" aria-live="polite" aria-atomic="true" aria-label="Archivo seleccionado">{fileName}</p>{/if}
      {#if imageError}<p class="image-error" role="alert">{imageError}</p>{/if}
    </aside>
  </div>
</AdminShell>

<style>
  .product-layout {
    margin-top: 35px;
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(280px, 340px);
    align-items: start;
    gap: 24px;
  }
  .product-form-card, .image-card {
    min-width: 0;
    border-radius: 18px;
    box-shadow: 0 5px 20px rgba(38, 22, 50, .06);
  }
  .product-form-card { padding: 28px; background: white; }
  .image-card { padding: 28px; background: #fce7f3; }
  h2 { margin: 0; color: var(--ink); font: 800 22px/1.25 'Nunito Variable', sans-serif; letter-spacing: -.02em; }
  form { margin-top: 26px; display: grid; gap: 20px; }
  label { min-width: 0; display: grid; gap: 8px; color: var(--ink); font-size: 14px; font-weight: 800; }
  input, textarea, select {
    width: 100%;
    min-width: 0;
    border: 1px solid #dfd3e8;
    border-radius: 12px;
    color: var(--ink);
    background: white;
    font: 600 15px/1.4 'Nunito Variable', sans-serif;
  }
  input, select { min-height: 48px; padding: 0 14px; }
  textarea { min-height: 112px; padding: 13px 14px; resize: vertical; }
  input:focus, textarea:focus, select:focus { border-color: var(--purple); outline: 0; box-shadow: 0 0 0 4px rgba(109, 40, 217, .12); }
  .field-row { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18px; }
  form button {
    width: min(220px, 100%);
    min-height: 50px;
    margin-top: 4px;
    border: 0;
    border-radius: 12px;
    color: white;
    background: var(--purple);
    font-weight: 800;
  }
  form button:hover { background: #5d1fc3; }
  .save-status { margin: 18px 0 0; padding: 13px 15px; border-radius: 12px; color: #3f176f; background: #f1e7ff; font-size: 14px; font-weight: 700; }
  .upload-zone {
    position: relative;
    min-height: 240px;
    margin-top: 26px;
    padding: 24px;
    place-content: center;
    border: 2px dashed #d9468f;
    border-radius: 16px;
    color: #86184f;
    background: rgba(255, 255, 255, .62);
    text-align: center;
    cursor: pointer;
  }
  .upload-zone::before { content: '↑'; display: block; margin-bottom: 12px; font-size: 34px; line-height: 1; }
  .upload-zone span { max-width: 230px; margin: 0 auto; font-weight: 800; }
  .upload-zone input { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden; clip: rect(0, 0, 0, 0); white-space: nowrap; border: 0; }
  .upload-zone:focus-within { outline: 3px solid #9d35c3; outline-offset: 5px; }
  .upload-zone.is-dragging { border-color: var(--purple); background: #f7edff; box-shadow: 0 0 0 5px rgba(109, 40, 217, .14); }
  .file-name { margin: 16px 0 0; overflow-wrap: anywhere; color: #731544; font-size: 14px; font-weight: 800; text-align: center; }
  .image-error { margin: 16px 0 0; color: #86184f; font-size: 14px; font-weight: 800; text-align: center; }

  @media (max-width: 1180px) {
    .product-layout { grid-template-columns: 1fr; }
    .image-card { order: 2; }
    .upload-zone { min-height: 190px; }
  }
  @media (max-width: 800px) {
    .product-layout { margin-top: 20px; gap: 16px; }
    .product-form-card, .image-card { padding: 22px; }
  }
  @media (max-width: 520px) {
    .product-form-card, .image-card { padding: 18px; border-radius: 15px; }
    .field-row { grid-template-columns: 1fr; gap: 20px; }
    h2 { font-size: 20px; }
    form button { width: 100%; }
    .upload-zone { min-height: 170px; padding: 18px; }
  }
</style>
