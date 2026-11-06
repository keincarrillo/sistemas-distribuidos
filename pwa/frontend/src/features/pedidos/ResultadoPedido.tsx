import { CheckCircle2 } from 'lucide-react';
import { Boton } from '@/components';
import styles from './ResultadoPedido.module.scss';
import type { ResultadoResponse } from './types';

interface ResultadoPedidoProps {
  resultado: ResultadoResponse;
  onOtroPedido: () => void;
}

/** Resultado confirmado por un worker del back (solo presenta datos). */
export default function ResultadoPedido({ resultado, onOtroPedido }: ResultadoPedidoProps) {
  return (
    <div className={styles.resultado} role="status">
      <div className={styles.resultado__cabecera}>
        <CheckCircle2 size={20} className={styles.resultado__icono} aria-hidden="true" />
        <p className={styles.resultado__titulo}>Pedido #{resultado.pedidoId} procesado</p>
      </div>
      <dl className={styles.resultado__datos}>
        <div>
          <dt>Producto</dt>
          <dd>{resultado.producto}</dd>
        </div>
        <div>
          <dt>Procesado</dt>
          <dd>{resultado.procesado ? 'Sí' : 'No'}</dd>
        </div>
        <div>
          <dt>Fecha</dt>
          <dd>{formatearFecha(resultado.procesadoEn)}</dd>
        </div>
      </dl>
      <div className={styles.resultado__acciones}>
        <Boton variante="secundario" onClick={onOtroPedido}>
          Hacer otro pedido
        </Boton>
      </div>
    </div>
  );
}

function formatearFecha(epochMs: number): string {
  if (!Number.isFinite(epochMs) || epochMs <= 0) {
    return '—';
  }
  return new Date(epochMs).toLocaleString();
}
