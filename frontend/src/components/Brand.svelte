<script lang="ts">
  import mark from '../assets/dulcepack-mark.webp';
  import wordmark from '../assets/dulcepack-wordmark.webp';

  let {
    href = '/',
    onclick,
    size = 'header',
    label = 'DulcePack, inicio',
  }: {
    href?: string;
    onclick?: (event: MouseEvent) => void;
    size?: 'header' | 'footer' | 'panel' | 'sidebar';
    label?: string;
  } = $props();
</script>

<a class="brand-lockup {size}" {href} {onclick} aria-label={label}>
  <img class="brand-mark" data-brand-part="mark" src={mark} alt="" aria-hidden="true" />
  <img class="brand-wordmark" data-brand-part="wordmark" src={wordmark} alt="DulcePack" />
</a>

<style>
  .brand-lockup {
    display: inline-flex;
    align-items: center;
    width: fit-content;
    max-width: 100%;
    text-decoration: none;
    line-height: 0;
  }
  img { display: block; height: auto; object-fit: contain; }
  .brand-mark { flex: 0 0 auto; }
  .brand-wordmark { min-width: 0; }

  .header { gap: 9px; }
  .header .brand-mark { width: 48px; }
  .header .brand-wordmark { width: 126px; }

  .footer { gap: 12px; padding: 10px 14px; border-radius: 16px; background: rgba(255, 255, 255, .94); }
  .footer .brand-mark { width: 58px; }
  .footer .brand-wordmark { width: 146px; }

  .panel { flex-direction: column; align-items: flex-start; gap: 18px; }
  .panel .brand-mark { width: min(300px, 100%); }
  .panel .brand-wordmark { width: min(320px, 100%); }

  .sidebar { gap: 9px; padding: 8px 10px; border-radius: 13px; background: rgba(255, 255, 255, .94); }
  .sidebar .brand-mark { width: 38px; }
  .sidebar .brand-wordmark { width: 112px; }

  @media (max-width: 600px) {
    .header { gap: 6px; }
    .header .brand-mark { width: 36px; }
    .header .brand-wordmark { width: 100px; }
    .footer .brand-mark { width: 48px; }
    .footer .brand-wordmark { width: 130px; }
    .panel { align-items: center; gap: 10px; }
    .panel .brand-mark { width: 150px; }
    .panel .brand-wordmark { width: 190px; }
  }
</style>
