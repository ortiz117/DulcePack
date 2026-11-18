<script lang="ts">
  import type { Product } from '../types/product';
  import Button from './Button.svelte';
  let { product, onadd, onselect, mobilePriority = false }: { product: Product; onadd: (product: Product) => void; onselect?: (product: Product) => void; mobilePriority?: boolean } = $props();
</script>

<article class="product-card" class:gomitas={product.id === 'gomitas'} class:mobile-priority={mobilePriority}>
  <a class="detail-link" href={`/producto/${product.id}`} aria-label={`Ver ${product.name}`} onclick={(event) => { if (onselect) { event.preventDefault(); onselect(product); } }}>
    <div class="product-art">
      <img src={product.image} alt="Ilustración del paquete Fiesta Mix, imagen de demostración" width="256" height="192" loading="lazy" />
      <span class="mobile-pack" aria-hidden="true"></span>
    </div>
    <h3 aria-label={product.name}><span class="desktop-name">{product.name}</span><span class="mobile-name">{product.shortName}</span></h3>
  </a>
  <p class="description">{product.description}</p>
  <p class="price">${product.price} <span>MXN</span></p>
  <Button label={`Agregar ${product.name} al carrito de demostración`} onclick={() => onadd(product)}><span class="desktop-name">Agregar al carrito</span><span class="mobile-name">Agregar</span></Button>
</article>

<style>
  .product-card { min-width: 0; background: white; border-radius: 20px; padding: 16px; box-shadow: 0 5px 18px #24123a08; min-height: 456px; }
  .product-art { border-radius: 16px; overflow: hidden; background: #fce7f3; }
  .detail-link { color: inherit; text-decoration: none; }
  .detail-link:hover h3 { color: var(--purple); }
  img { display: block; width: 100%; height: auto; aspect-ratio: 4 / 3; object-fit: cover; }
  h3 { font: 600 22px/1.3 'Fredoka Variable', sans-serif; margin: 14px 0 8px; }
  .description { font-size: 14px; color: var(--muted); margin: 0 0 14px; }
  .price { font-size: 20px; line-height: 1.3; font-weight: 800; color: var(--purple); margin: 0 0 12px; }
  .mobile-name, .mobile-pack { display: none; }
  @media (max-width: 1050px) { h3 { font-size: 20px; } }
  @media (max-width: 600px) {
    .product-card { min-height: 252px; padding: 12px; border-radius: 18px; box-shadow: none; }
    .product-art { height: 112px; display: grid; place-items: center; border-radius: 12px; }
    .product-art img { display: none; }
    .mobile-pack { display: block; background: var(--purple); width: 58px; height: 78px; border-radius: 9px; }
    h3 { font: 400 14px/1.35 'Nunito Variable', sans-serif; margin: 10px 0 8px; }
    .price { font-size: 16px; margin-bottom: 8px; font-weight: 700; }
    .description, .desktop-name, .price span { display: none; }
    .mobile-name { display: inline; }
    .mobile-priority.gomitas { order: -1; }
    .mobile-priority:first-child { order: -2; }
  }
</style>
