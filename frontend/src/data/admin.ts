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
