import { http } from '@/lib/http';
import type { PedidoRequest, ResultadoResponse } from './types';

/** Fachada del dominio pedidos: el resto del front no conoce la URL. */
export function crearPedido(pedido: PedidoRequest): Promise<ResultadoResponse> {
  return http.post<ResultadoResponse>('/api/pedidos', pedido);
}
