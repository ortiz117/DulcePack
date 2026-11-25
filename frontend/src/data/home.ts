import type { Category, Product } from '../types/product';
import productImage from '../assets/fiesta-mix.webp';

// Datos de demostración transcritos del diseño. No son inventario ni ofertas reales.
// La taxonomía coincide con la sembrada por V2__catalog_categories.sql.
export const categories: readonly Category[] = [
  { id: 'botanas', slug: 'botanas', name: 'Botanas', shortName: 'Botanas' },
  { id: 'dulces', slug: 'dulces', name: 'Dulces', shortName: 'Dulces' },
  { id: 'paquetes', slug: 'paquetes', name: 'Paquetes', shortName: 'Paquetes' },
];

export const featuredProducts: readonly Product[] = [
  { id: 'fiesta', name: 'Pack Fiesta Dulce', shortName: 'Pack Fiesta', description: 'El favorito para compartir.', price: 189, currency: 'MXN', category: 'paquetes', kind: 'paquetes', flavor: 'dulce', image: productImage },
  { id: 'picosita', name: 'Box Picosita', shortName: 'Box Picosita', description: 'Chamoy, tamarindo y chile.', price: 159, currency: 'MXN', category: 'dulces', kind: 'dulces', flavor: 'picante', image: productImage },
  { id: 'gomitas', name: 'Mix Gomitas', shortName: 'Mix Gomitas', description: 'Suaves, ácidas y frutales.', price: 129, currency: 'MXN', category: 'dulces', kind: 'dulces', flavor: 'acido', image: productImage },
  { id: 'pelis', name: 'Noche de Pelis', shortName: 'Noche de Pelis', description: 'Palomitas, chocolate y más.', price: 219, currency: 'MXN', category: 'botanas', kind: 'botanas', flavor: 'dulce', image: productImage },
];

export const catalogProducts: readonly Product[] = [
  ...featuredProducts,
  { id: 'fiesta-mini', name: 'Pack Fiesta Mini', shortName: 'Fiesta Mini', description: 'Una selección pequeña para consentirte.', price: 99, currency: 'MXN', category: 'paquetes', kind: 'paquetes', flavor: 'dulce', image: productImage },
  { id: 'botana-mix', name: 'Botana Mix', shortName: 'Botana Mix', description: 'Crujientes, saladas y listas para compartir.', price: 149, currency: 'MXN', category: 'botanas', kind: 'botanas', flavor: 'picante', image: productImage },
];
