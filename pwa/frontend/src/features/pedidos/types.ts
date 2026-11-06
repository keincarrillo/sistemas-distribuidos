/** DTO que espejea `PedidoRequest` del back. */
export interface PedidoRequest {
  producto: string;
  cliente: string;
}

/** DTO que espejea `ResultadoResponse` del back. */
export interface ResultadoResponse {
  pedidoId: number;
  producto: string;
  procesado: boolean;
  procesadoEn: number;
}

/** Catálogo de productos válido para el back. */
export const CATALOGO = ['Laptop', 'Mouse', 'Teclado', 'Monitor', 'USB'] as const;
