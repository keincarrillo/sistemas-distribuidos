import { useEffect, useRef } from 'react';

/**
 * Ejecuta `callback` cada `delay` ms (patrón Observer/relé).
 * Usa un ref para no reiniciar el intervalo en cada render.
 * `delay = null` pausa el intervalo.
 */
export function useInterval(callback: () => void, delay: number | null): void {
  const callbackRef = useRef(callback);

  useEffect(() => {
    callbackRef.current = callback;
  }, [callback]);

  useEffect(() => {
    if (delay === null) {
      return undefined;
    }
    const id = setInterval(() => callbackRef.current(), delay);
    return () => clearInterval(id);
  }, [delay]);
}
