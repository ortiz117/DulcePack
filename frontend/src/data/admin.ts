export type AdminMetric = {
  label: string;
  value: string;
  helper: string;
  tone: 'purple' | 'pink' | 'yellow';
};

export type WeeklySale = {
  day: string;
  label: string;
  value: number;
};

export const adminMetrics: AdminMetric[] = [
  { label: 'Ventas del mes', value: '$24,580', helper: '+12%', tone: 'purple' },
  { label: 'Pedidos', value: '128', helper: '+8%', tone: 'pink' },
  { label: 'Productos activos', value: '36', helper: '3 con stock bajo', tone: 'yellow' },
];

export const weeklySales: WeeklySale[] = [
  { day: 'L', label: 'Lunes', value: 140 },
  { day: 'M', label: 'Martes', value: 210 },
  { day: 'X', label: 'Miércoles', value: 170 },
  { day: 'J', label: 'Jueves', value: 260 },
  { day: 'V', label: 'Viernes', value: 230 },
  { day: 'S', label: 'Sábado', value: 278 },
  { day: 'D', label: 'Domingo', value: 190 },
];

export const adminSections = ['Resumen', 'Productos', 'Inventario', 'Pedidos', 'Usuarios'] as const;

export type AdminProduct = {
  id: string;
  name: string;
  category: 'Paquetes' | 'Dulces' | 'Botanas';
  price: number;
  stock: number;
  status: 'Activo' | 'Bajo';
};

export const adminProducts: AdminProduct[] = [
  { id: 'fiesta-dulce', name: 'Pack Fiesta Dulce', category: 'Paquetes', price: 189, stock: 24, status: 'Activo' },
  { id: 'box-picosita', name: 'Box Picosita', category: 'Paquetes', price: 159, stock: 7, status: 'Bajo' },
  { id: 'mix-gomitas', name: 'Mix Gomitas', category: 'Dulces', price: 129, stock: 38, status: 'Activo' },
  { id: 'noche-pelis', name: 'Noche de Pelis', category: 'Botanas', price: 219, stock: 15, status: 'Activo' },
  { id: 'pack-cumpleanos', name: 'Pack Cumpleaños', category: 'Paquetes', price: 289, stock: 4, status: 'Bajo' },
  { id: 'choco-lovers', name: 'Choco Lovers', category: 'Dulces', price: 199, stock: 18, status: 'Activo' },
];
