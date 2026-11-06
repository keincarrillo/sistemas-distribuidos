import { http } from '@/lib/http';
import type { EstadoResponse, SaludResponse } from './types';

/** Fachada del dominio estado: estadísticas del servicio. */
export function obtenerEstado(): Promise<EstadoResponse> {
  return http.get<EstadoResponse>('/api/estado');
}

/** Health check del servicio (lo usa el panel de estado). */
export function consultarSalud(): Promise<SaludResponse> {
  return http.get<SaludResponse>('/health');
}
