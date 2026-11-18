import type { Category, Product } from '../types/product';
import productImage from '../assets/fiesta-mix.webp';

// Datos de demostración transcritos del diseño. No son inventario ni ofertas reales.
export const categories: readonly Category[] = [
  { id: 'picositos', name: 'Dulces picositos', shortName: 'Picositos' },
  { id: 'gomitas', name: 'Gomitas', shortName: 'Gomitas' },
  { id: 'chocolates', name: 'Chocolates', shortName: 'Chocolates' },
  { id: 'botanas', name: 'Botanas', shortName: 'Botanas' },
];

export const featuredProducts: readonly Product[] = [
  { id: 'fiesta', name: 'Pack Fiesta Dulce', shortName: 'Pack Fiesta', description: 'El favorito para compartir.', price: 189, currency: 'MXN', category: 'chocolates', image: productImage },
  { id: 'picosita', name: 'Box Picosita', shortName: 'Box Picosita', description: 'Chamoy, tamarindo y chile.', price: 159, currency: 'MXN', category: 'picositos', image: productImage },
  { id: 'gomitas', name: 'Mix Gomitas', shortName: 'Mix Gomitas', description: 'Suaves, ácidas y frutales.', price: 129, currency: 'MXN', category: 'gomitas', image: productImage },
  { id: 'pelis', name: 'Noche de Pelis', shortName: 'Noche de Pelis', description: 'Palomitas, chocolate y más.', price: 219, currency: 'MXN', category: 'botanas', image: productImage },
];
