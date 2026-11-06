/** DTO que espejea `EstadoResponse` del back. */
export interface EstadoResponse {
  estado: string;
  colaActual: number;
  capacidadCola: number;
  almacenDisponible: number;
  capacidadAlmacen: number;
  peticionesActivas: number;
  peticionesAtendidas: number;
  peticionesRechazadas: number;
  pedidosProcesados: number;
  workers: number;
}

/** DTO que espejea la respuesta de `GET /health`. */
export interface SaludResponse {
  status: string;
}
