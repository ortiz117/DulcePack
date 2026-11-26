export type CategoryId = 'dulces' | 'botanas' | 'paquetes';

export interface Product {
  id: string;
  name: string;
  shortName: string;
  description: string;
  price: number;
  currency: 'MXN';
  category: CategoryId;
  kind?: CategoryId;
  flavor?: 'dulce' | 'acido' | 'picante';
  image: string;
  available?: number;
  stockStatus?: 'AVAILABLE' | 'LOW' | 'OUT_OF_STOCK';
  tags?: string[];
}

export interface Category {
  id: CategoryId;
  slug: CategoryId;
  name: string;
  shortName: string;
}

export interface CartItem {
  product: Product;
  quantity: number;
}
