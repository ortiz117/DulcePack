<script lang="ts">
  import Home from './pages/Home.svelte';
  import Catalog from './pages/Catalog.svelte';
  import ProductDetail from './pages/ProductDetail.svelte';
  import Cart from './pages/Cart.svelte';
  import Checkout from './pages/Checkout.svelte';
  import Confirmation from './pages/Confirmation.svelte';
  import Login from './pages/Login.svelte';
  import Register from './pages/Register.svelte';
  import AdminLogin from './pages/AdminLogin.svelte';
  import AdminDashboard from './pages/AdminDashboard.svelte';
  import AdminProducts from './pages/AdminProducts.svelte';
  import AdminOrders from './pages/AdminOrders.svelte';
  import AdminUsers from './pages/AdminUsers.svelte';
  import type { CartItem, Product } from './types/product';

  const isProtectedAdminPath = (value: string) => value.startsWith('/admin/');
  const initialPath = isProtectedAdminPath(window.location.pathname) && sessionStorage.getItem('dulcepack-admin-session') !== 'active'
    ? '/admin'
    : window.location.pathname;
  if (initialPath !== window.location.pathname) history.replaceState({}, '', initialPath);
  let path = $state(initialPath);
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

  function login(email: string) {
    const name = email.split('@')[0].trim().toLocaleLowerCase('es-MX');
    sessionStorage.setItem('dulcepack-user-name', name || 'cliente');
    navigate('/');
  }

  function register(name: string) {
    sessionStorage.setItem('dulcepack-user-name', name.trim().split(/\s+/)[0] || 'cliente');
    navigate('/');
  }

  function navigate(nextPath: string) {
    const allowedPath = isProtectedAdminPath(nextPath) && sessionStorage.getItem('dulcepack-admin-session') !== 'active'
      ? '/admin'
      : nextPath;
    history.pushState({}, '', allowedPath);
    path = allowedPath;
    window.scrollTo({ top: 0, behavior: 'instant' });
  }

  window.addEventListener('popstate', () => {
    const nextPath = window.location.pathname;
    if (isProtectedAdminPath(nextPath) && sessionStorage.getItem('dulcepack-admin-session') !== 'active') {
      history.replaceState({}, '', '/admin');
      path = '/admin';
      return;
    }
    path = nextPath;
  });
</script>

{#if path === '/admin/usuarios'}
  <AdminUsers {navigate} />
{:else if path === '/admin/pedidos'}
  <AdminOrders {navigate} />
{:else if path === '/admin/productos'}
  <AdminProducts {navigate} />
{:else if path === '/admin/dashboard'}
  <AdminDashboard {navigate} />
{:else if path === '/admin'}
  <AdminLogin {navigate} />
{:else if path === '/registro'}
  <Register {navigate} {register} />
{:else if path === '/iniciar-sesion'}
  <Login {navigate} {login} />
{:else if path === '/confirmacion'}
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
