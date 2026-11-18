export type CategoryId = 'picositos' | 'gomitas' | 'chocolates' | 'botanas';

export interface Product {
  id: string;
  name: string;
  shortName: string;
  description: string;
  price: number;
  currency: 'MXN';
  category: CategoryId;
  image: string;
}

export interface Category {
  id: CategoryId;
  name: string;
  shortName: string;
}
