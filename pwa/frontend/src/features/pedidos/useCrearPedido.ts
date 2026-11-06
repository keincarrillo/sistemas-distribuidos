import { useState } from 'react';
import { ApiError, mensajeDeError } from '@/lib/errores';
import { crearPedido } from './api';
import type { PedidoRequest, ResultadoResponse } from './types';

type EstadoPedido =
  | { fase: 'inicial' }
  | { fase: 'enviando' }
  | { fase: 'ok'; resultado: ResultadoResponse }
  | { fase: 'error'; mensaje: string; codigo?: number };

/** Hook con la máquina de estados de un pedido (enviar, resultado, error). */
export function useCrearPedido() {
  const [estado, setEstado] = useState<EstadoPedido>({ fase: 'inicial' });

  const enviar = async (pedido: PedidoRequest): Promise<void> => {
    setEstado({ fase: 'enviando' });
    try {
      const resultado = await crearPedido(pedido);
      setEstado({ fase: 'ok', resultado });
    } catch (error) {
      setEstado({
        fase: 'error',
        mensaje: mensajeDeError(error),
        codigo: error instanceof ApiError ? error.codigo : undefined,
      });
    }
  };

  const reiniciar = (): void => setEstado({ fase: 'inicial' });

  return { estado, enviar, reiniciar };
}
