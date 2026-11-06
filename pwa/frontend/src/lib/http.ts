import { ApiError, ErrorRed } from './errores';

interface OpcionesPeticion {
  method?: 'GET' | 'POST';
  cuerpo?: unknown;
}

interface CuerpoError {
  error?: unknown;
}

/**
 * Fachada HTTP: los componentes nunca llaman a `fetch` directamente,
 * usan `http.get` / `http.post`. Centraliza JSON, errores de red y
 * el cuerpo de error uniforme del back (`{ "error": "..." }`).
 */
export const http = {
  get: <T>(ruta: string): Promise<T> => pedir<T>(ruta),
  post: <T>(ruta: string, cuerpo: unknown): Promise<T> =>
    pedir<T>(ruta, { method: 'POST', cuerpo }),
};

async function pedir<T>(ruta: string, opciones: OpcionesPeticion = {}): Promise<T> {
  const { method = 'GET', cuerpo } = opciones;

  let respuesta: Response;
  try {
    respuesta = await fetch(ruta, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
    });
  } catch {
    throw new ErrorRed();
  }

  if (!respuesta.ok) {
    throw await aApiError(respuesta);
  }

  return (await respuesta.json()) as T;
}

async function aApiError(respuesta: Response): Promise<ApiError> {
  let mensaje = `El servidor respondió con HTTP ${respuesta.status}`;

  try {
    const cuerpo = (await respuesta.json()) as CuerpoError;
    if (typeof cuerpo.error === 'string' && cuerpo.error.length > 0) {
      mensaje = cuerpo.error;
    }
  } catch {
    // Cuerpo no JSON: se mantiene el mensaje genérico.
  }

  return new ApiError(respuesta.status, mensaje);
}
