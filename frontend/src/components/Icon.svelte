<script lang="ts">
  let { name }: { name: 'search' | 'cart' | 'user' | 'arrow' | 'check' } = $props();
</script>

<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
  {#if name === 'search'}
    <circle cx="10.5" cy="10.5" r="6.5" /><path d="m16 16 4.5 4.5" />
  {:else if name === 'cart'}
    <path d="M2 3h3l2.5 12h11l2-8H6" /><circle cx="9" cy="20" r="1" /><circle cx="18" cy="20" r="1" />
  {:else if name === 'user'}
    <circle cx="12" cy="8" r="4" /><path d="M4 21v-2a8 8 0 0 1 16 0v2" />
  {:else if name === 'arrow'}
    <path d="M4 12h16m-6-6 6 6-6 6" />
  {:else}
    <path d="m5 12 4 4L19 6" />
  {/if}
</svg>
