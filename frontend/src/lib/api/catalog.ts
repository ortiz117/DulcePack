import type { Category, CategoryId, Product } from '../../types/product';

export const API_BASE_URL: string =
  (globalThis as { __DULCEPACK_API__?: string }).__DULCEPACK_API__ ?? 'http://localhost:8080';

export interface CatalogProductResponse {
  id: string;
  name: string;
  shortName: string;
  description: string;
  price: number;
  currency: 'MXN';
  category: string;
  kind: string;
  flavor?: string | null;
  imageUrl?: string | null;
  available: number;
  stockStatus: 'AVAILABLE' | 'LOW' | 'OUT_OF_STOCK';
  tags: string[];
}

export interface CategoryResponse {
  id: string;
  slug: string;
  name: string;
  shortName: string;
}

export interface CatalogPage<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CatalogFilters {
  query?: string;
  category?: CategoryId;
  flavor?: 'dulce' | 'acido' | 'picante';
  minPrice?: number;
  maxPrice?: number;
}

export function toProduct(response: CatalogProductResponse): Product {
  return {
    id: response.id,
    name: response.name,
    shortName: response.shortName,
    description: response.description,
    price: response.price,
    currency: response.currency,
    category: response.category as CategoryId,
    kind: response.kind as CategoryId,
    flavor: (response.flavor ?? undefined) as Product['flavor'],
    image: response.imageUrl ?? '',
    available: response.available,
    stockStatus: response.stockStatus,
    tags: response.tags,
  };
}

export function toCategory(response: CategoryResponse): Category {
  return {
    id: response.id as CategoryId,
    slug: response.slug as CategoryId,
    name: response.name,
    shortName: response.shortName,
  };
}

function queryString(filters: CatalogFilters): string {
  const params = new URLSearchParams();
  if (filters.query) params.set('query', filters.query);
  if (filters.category) params.set('category', filters.category);
  if (filters.flavor) params.set('flavor', filters.flavor);
  if (filters.minPrice !== undefined) params.set('minPrice', String(filters.minPrice));
  if (filters.maxPrice !== undefined) params.set('maxPrice', String(filters.maxPrice));
  const value = params.toString();
  return value ? `?${value}` : '';
}

async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, { credentials: 'include' });
  if (!response.ok) throw new Error(`DulcePack API responded ${response.status}`);
  return (await response.json()) as T;
}

export async function fetchCategories(): Promise<Category[]> {
  const responses = await getJson<CategoryResponse[]>('/api/catalog/categories');
  return responses.map(toCategory);
}

export async function searchProducts(filters: CatalogFilters = {}): Promise<Product[]> {
  const responses = await getJson<CatalogProductResponse[]>(`/api/catalog/products${queryString(filters)}`);
  return responses.map(toProduct);
}

export async function fetchProduct(slug: string): Promise<Product> {
  return toProduct(await getJson<CatalogProductResponse>(`/api/catalog/products/${encodeURIComponent(slug)}`));
}

export function productImageUrl(slug: string): string {
  return `${API_BASE_URL}/api/catalog/products/${encodeURIComponent(slug)}/image`;
}
