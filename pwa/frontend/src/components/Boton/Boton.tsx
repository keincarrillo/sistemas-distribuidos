import type { ButtonHTMLAttributes, ReactNode } from 'react';
import styles from './Boton.module.scss';

type Variante = 'primario' | 'secundario' | 'fantasma';

interface BotonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variante?: Variante;
  icono?: ReactNode;
}

/** Botón reutilizable con variantes (estrategy) e icono opcional. */
export default function Boton({
  variante = 'primario',
  icono,
  children,
  className,
  ...resto
}: BotonProps) {
  const clases = [styles.boton, styles[`boton--${variante}`], className].filter(Boolean).join(' ');

  return (
    <button type="button" className={clases} {...resto}>
      {icono && <span className={styles.boton__icono}>{icono}</span>}
      {children}
    </button>
  );
}
