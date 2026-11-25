import { API_BASE_URL } from './catalog';

export interface CartItem {
  productSlug: string;
  name: string;
  imageUrl: string | null;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
  available: number;
}

export interface CartSummary {
  items: CartItem[];
  itemCount: number;
  subtotal: number;
  shipping: number;
  discount: number;
  total: number;
  couponCode: string | null;
}

export interface AddCartItemPayload {
  productSlug: string;
  quantity: number;
}

function csrfHeader(): HeadersInit {
  // Con CsrfTokenRequestAttributeHandler el valor de la cookie XSRF-TOKEN es el
  // token que el servidor valida en X-XSRF-TOKEN.
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
  const token = match ? decodeURIComponent(match[1]) : '';
  return token ? { 'X-XSRF-TOKEN': token } : {};
}

async function mutate(path: string, init: RequestInit): Promise<CartSummary> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: 'include',
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...csrfHeader(),
      ...(init.headers ?? {}),
    },
  });
  if (!response.ok) throw new Error(`DulcePack cart API responded ${response.status}`);
  return (await response.json()) as CartSummary;
}

export function fetchCart(): Promise<CartSummary> {
  return fetch(`${API_BASE_URL}/api/cart`, { credentials: 'include' }).then((response) => {
    if (!response.ok) throw new Error(`DulcePack cart API responded ${response.status}`);
    return response.json() as Promise<CartSummary>;
  });
}

export function addCartItem(payload: AddCartItemPayload): Promise<CartSummary> {
  return mutate('/api/cart/items', { method: 'POST', body: JSON.stringify(payload) });
}

export function updateCartItem(productSlug: string, quantity: number): Promise<CartSummary> {
  return mutate(`/api/cart/items/${encodeURIComponent(productSlug)}`, {
    method: 'PATCH',
    body: JSON.stringify({ quantity }),
  });
}

export function removeCartItem(productSlug: string): Promise<CartSummary> {
  return mutate(`/api/cart/items/${encodeURIComponent(productSlug)}`, { method: 'DELETE' });
}

export function applyCoupon(code: string): Promise<CartSummary> {
  return mutate('/api/cart/coupon', { method: 'POST', body: JSON.stringify({ code }) });
}

export function clearCoupon(): Promise<CartSummary> {
  return mutate('/api/cart/coupon', { method: 'DELETE' });
}
