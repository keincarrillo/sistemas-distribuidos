import type { ReactNode } from 'react';
import styles from './EstadoPill.module.scss';

type Tono = 'ok' | 'error' | 'aviso' | 'neutro';

interface EstadoPillProps {
  tono: Tono;
  children: ReactNode;
}

/** Chip de estado reutilizable para resultados y estados del sistema. */
export default function EstadoPill({ tono, children }: EstadoPillProps) {
  return <span className={`${styles.pill} ${styles[`pill--${tono}`]}`}>{children}</span>;
}
