import { useCallback, useEffect, useState } from 'react';
import { useInterval } from '@/hooks/useInterval';
import { mensajeDeError } from '@/lib/errores';
import { consultarSalud, obtenerEstado } from './api';
import type { EstadoResponse } from './types';

const INTERVALO_MS = 4000;

interface EstadoServicio {
  datos: EstadoResponse | null;
  salud: string | null;
  cargando: boolean;
  error: string | null;
  recargar: () => void;
}

/**
 * Polling del estado del back cada 4 s + health check.
 * Si la API cae, conserva los últimos datos y expone el error.
 */
export function useEstado(): EstadoServicio {
  const [datos, setDatos] = useState<EstadoResponse | null>(null);
  const [salud, setSalud] = useState<string | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const consultar = useCallback(async (): Promise<void> => {
    try {
      const [estado, saludRespuesta] = await Promise.all([obtenerEstado(), consultarSalud()]);
      setDatos(estado);
      setSalud(saludRespuesta.status);
      setError(null);
    } catch (capturado) {
      setError(mensajeDeError(capturado));
    } finally {
      setCargando(false);
    }
  }, []);

  const recargar = useCallback((): void => {
    setCargando(true);
    void consultar();
  }, [consultar]);

  useEffect(() => {
    // Carga inicial del estado: la consulta es asíncrona y el estado se
    // actualiza tras el await (el primer render solo muestra el spinner).
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void consultar();
  }, [consultar]);

  useInterval(() => void consultar(), INTERVALO_MS);

  return { datos, salud, cargando, error, recargar };
}
