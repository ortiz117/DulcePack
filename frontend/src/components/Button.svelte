<script lang="ts">
  import type { Snippet } from 'svelte';
  let { children, onclick, label, variant = 'primary' }: {
    children: Snippet;
    onclick?: () => void;
    label?: string;
    variant?: 'primary' | 'pink';
  } = $props();
</script>

<button type="button" class="button {variant}" aria-label={label} {onclick}>{@render children()}</button>

<style>
  .button { min-height: 48px; border: 0; border-radius: 12px; padding: 12px 30px; background: var(--purple); color: white; font-size: 14px; font-weight: 700; transition: background .18s, transform .18s; }
  .button:hover { background: #5620b5; transform: translateY(-1px); }
  .pink { background: var(--pink); color: var(--ink); }
  .pink:hover { background: #f66cb0; }
  @media (max-width: 600px) {
    .button { width: 100%; min-height: 38px; border-radius: 12px; padding: 8px 10px; font-size: 13px; background: var(--yellow); color: var(--ink); }
    .button:hover { background: #f5b50b; }
    .pink { background: white; color: var(--purple); }
  }
</style>
