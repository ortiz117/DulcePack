<script lang="ts">
  import Home from './pages/Home.svelte';
  import Catalog from './pages/Catalog.svelte';
  import ProductDetail from './pages/ProductDetail.svelte';
  import Cart from './pages/Cart.svelte';
  import Checkout from './pages/Checkout.svelte';
  import Confirmation from './pages/Confirmation.svelte';
  import type { CartItem, Product } from './types/product';

  let path = $state(window.location.pathname);
  let cart = $state<CartItem[]>([]);
  let cartCount = $derived(cart.reduce((total, item) => total + item.quantity, 0));
  let subtotal = $derived(cart.reduce((total, item) => total + item.product.price * item.quantity, 0));
  let shipping = $derived(cart.length ? 79 : 0);
  let discount = $derived(cart.length ? 50 : 0);
  let orderTotal = $derived(Math.max(0, subtotal + shipping - discount));

  function addToCart(product: Product, quantity = 1) {
    const existing = cart.find(item => item.product.id === product.id);
    cart = existing
      ? cart.map(item => item.product.id === product.id ? { ...item, quantity: item.quantity + quantity } : item)
      : [...cart, { product, quantity }];
  }

  function updateCart(productId: string, quantity: number) {
    cart = cart.map(item => item.product.id === productId ? { ...item, quantity: Math.max(1, quantity) } : item);
  }

  function removeFromCart(productId: string) {
    cart = cart.filter(item => item.product.id !== productId);
  }

  function completeOrder() {
    cart = [];
    navigate('/confirmacion');
  }

  function navigate(nextPath: string) {
    history.pushState({}, '', nextPath);
    path = nextPath;
    window.scrollTo({ top: 0, behavior: 'instant' });
  }

  window.addEventListener('popstate', () => path = window.location.pathname);
</script>

{#if path === '/confirmacion'}
  <Confirmation {navigate} {cartCount} />
{:else if path === '/checkout'}
  <Checkout {cart} {cartCount} {subtotal} {shipping} {discount} {orderTotal} {navigate} {completeOrder} />
{:else if path === '/carrito'}
  <Cart {cart} {cartCount} {navigate} {updateCart} {removeFromCart} />
{:else if path.startsWith('/producto/')}
  <ProductDetail productId={path.split('/').pop() ?? ''} {navigate} {cartCount} {addToCart} />
{:else if path === '/catalogo'}
  <Catalog {navigate} {cartCount} {addToCart} />
{:else}
  <Home {navigate} {cartCount} {addToCart} />
{/if}
