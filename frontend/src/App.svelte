<script lang="ts">
  import Home from './pages/Home.svelte';
  import Catalog from './pages/Catalog.svelte';
  import ProductDetail from './pages/ProductDetail.svelte';

  let path = $state(window.location.pathname);

  function navigate(nextPath: string) {
    history.pushState({}, '', nextPath);
    path = nextPath;
    window.scrollTo({ top: 0, behavior: 'instant' });
  }

  window.addEventListener('popstate', () => path = window.location.pathname);
</script>

{#if path.startsWith('/producto/')}
  <ProductDetail productId={path.split('/').pop() ?? ''} {navigate} />
{:else if path === '/catalogo'}
  <Catalog {navigate} />
{:else}
  <Home {navigate} />
{/if}
