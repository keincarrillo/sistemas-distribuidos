/**
 * Errores de dominio de la capa de red.
 * Equivalente en el front a las excepciones del back (ProductoInvalidoException...).
 */

/** La API respondió con un código HTTP de error (400, 503, 504...). */
export class ApiError extends Error {
  readonly codigo: number;

  constructor(codigo: number, mensaje: string) {
    super(mensaje);
    this.name = 'ApiError';
    this.codigo = codigo;
  }
}

/** No hubo respuesta del servidor (sin conexión o servicio caído). */
export class ErrorRed extends Error {
  constructor(mensaje = 'No se pudo conectar con el servidor') {
    super(mensaje);
    this.name = 'ErrorRed';
  }
}

/** Traduce cualquier error a un mensaje apto para la UI. */
export function mensajeDeError(error: unknown): string {
  if (error instanceof ApiError || error instanceof ErrorRed) {
    return error.message;
  }
  return 'Error inesperado';
}
