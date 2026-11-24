export type CategoryId = 'picositos' | 'gomitas' | 'chocolates' | 'botanas';

export interface Product {
  id: string;
  name: string;
  shortName: string;
  description: string;
  price: number;
  currency: 'MXN';
  category: CategoryId;
  kind?: 'dulces' | 'botanas' | 'paquetes';
  flavor?: 'dulce' | 'acido' | 'picante';
  image: string;
}

export interface Category {
  id: CategoryId;
  name: string;
  shortName: string;
}

export interface CartItem {
  product: Product;
  quantity: number;
}
