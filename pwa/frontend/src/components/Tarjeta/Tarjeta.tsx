import type { ReactNode } from 'react';
import styles from './Tarjeta.module.scss';

interface TarjetaProps {
  titulo: string;
  accion?: ReactNode;
  children: ReactNode;
}

/** Contenedor visual reutilizable: cabecera con título (+ acción) y cuerpo. */
export default function Tarjeta({ titulo, accion, children }: TarjetaProps) {
  return (
    <section className={styles.tarjeta}>
      <header className={styles.tarjeta__cabecera}>
        <h2 className={styles.tarjeta__titulo}>{titulo}</h2>
        {accion && <div className={styles.tarjeta__accion}>{accion}</div>}
      </header>
      <div className={styles.tarjeta__cuerpo}>{children}</div>
    </section>
  );
}
