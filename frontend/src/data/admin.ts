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

export type InventoryMetric = {
  label: string;
  value: number;
  tone: 'purple' | 'pink' | 'ink';
};

export type InventoryItem = {
  product: string;
  available: number;
  minimum: number;
  status: 'Disponible' | 'Bajo';
  updatedAt: string;
};

export const inventoryMetrics: InventoryMetric[] = [
  { label: 'Unidades disponibles', value: 436, tone: 'purple' },
  { label: 'Stock bajo', value: 3, tone: 'pink' },
  { label: 'Sin existencias', value: 1, tone: 'ink' },
];

export const inventoryItems: InventoryItem[] = [
  { product: 'Pack Fiesta Dulce', available: 24, minimum: 10, status: 'Disponible', updatedAt: 'Hoy, 08:30' },
  { product: 'Box Picosita', available: 7, minimum: 10, status: 'Bajo', updatedAt: 'Ayer' },
  { product: 'Mix Gomitas', available: 38, minimum: 12, status: 'Disponible', updatedAt: 'Hoy, 07:10' },
  { product: 'Pack Cumpleaños', available: 4, minimum: 8, status: 'Bajo', updatedAt: 'Ayer' },
  { product: 'Choco Lovers', available: 18, minimum: 8, status: 'Disponible', updatedAt: '2 nov' },
];

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

export type AdminOrderItem = {
  product: string;
  quantity: number;
  unitPrice: number;
};

export type AdminOrder = {
  id: string;
  customer: string;
  email: string;
  address: string;
  items: AdminOrderItem[];
  shipping: number;
  total: number;
  date: string;
  status: 'Pendiente' | 'Preparando' | 'Enviado' | 'Entregado' | 'Cancelado';
};

export const adminOrders: AdminOrder[] = [
  { id: 'DP-1048', customer: 'Isaac Ortiz', email: 'isaac@email.com', address: 'Av. Universidad 120, Col. Centro', items: [{ product: 'Pack Fiesta Dulce', quantity: 2, unitPrice: 189 }, { product: 'Box Picosita', quantity: 1, unitPrice: 159 }], shipping: 98, total: 635, date: '6 nov', status: 'Pendiente' },
  { id: 'DP-1047', customer: 'Ana López', email: 'ana@email.com', address: 'Calle Primavera 42, Col. Jardines', items: [{ product: 'Pack Fiesta Dulce', quantity: 1, unitPrice: 189 }, { product: 'Box Picosita', quantity: 1, unitPrice: 159 }], shipping: 0, total: 348, date: '6 nov', status: 'Preparando' },
  { id: 'DP-1046', customer: 'Luis García', email: 'luis@email.com', address: 'Paseo del Lago 18, Col. Las Flores', items: [{ product: 'Noche de Pelis', quantity: 1, unitPrice: 219 }], shipping: 0, total: 219, date: '5 nov', status: 'Enviado' },
  { id: 'DP-1045', customer: 'Karla Díaz', email: 'karla@email.com', address: 'Calle Naranjo 75, Col. Alameda', items: [{ product: 'Pack Fiesta Dulce', quantity: 1, unitPrice: 189 }], shipping: 0, total: 189, date: '5 nov', status: 'Entregado' },
  { id: 'DP-1044', customer: 'Marco Ruiz', email: 'marco@email.com', address: 'Av. Reforma 330, Col. Moderna', items: [{ product: 'Box Picosita', quantity: 2, unitPrice: 159 }, { product: 'Mix Gomitas', quantity: 1, unitPrice: 129 }], shipping: 0, total: 447, date: '4 nov', status: 'Cancelado' },
  { id: 'DP-1043', customer: 'Sofía Pérez', email: 'sofia@email.com', address: 'Calle Luna 16, Col. Mirador', items: [{ product: 'Mix Gomitas', quantity: 1, unitPrice: 129 }, { product: 'Selección de snacks', quantity: 1, unitPrice: 50 }], shipping: 79, total: 258, date: '4 nov', status: 'Entregado' },
];

export type AdminUser = {
  name: string;
  email: string;
  role: 'Cliente' | 'Administrador';
  orders: number | null;
  status: 'Activo' | 'Suspendido';
};

export const adminUsers: AdminUser[] = [
  { name: 'Isaac Ortiz', email: 'isaac@email.com', role: 'Cliente', orders: 6, status: 'Activo' },
  { name: 'Ana López', email: 'ana@email.com', role: 'Cliente', orders: 3, status: 'Activo' },
  { name: 'Luis García', email: 'luis@email.com', role: 'Cliente', orders: 2, status: 'Activo' },
  { name: 'María Admin', email: 'maria@dulcepack.mx', role: 'Administrador', orders: null, status: 'Activo' },
  { name: 'Marco Ruiz', email: 'marco@email.com', role: 'Cliente', orders: 1, status: 'Suspendido' },
];
