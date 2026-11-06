import styles from './Spinner.module.scss';

interface SpinnerProps {
  etiqueta?: string;
}

/** Indicador de carga accesible (role="status") reutilizable. */
export default function Spinner({ etiqueta = 'Cargando…' }: SpinnerProps) {
  return (
    <div className={styles.envoltura} role="status">
      <span className={styles.circulo} aria-hidden="true" />
      <span className={styles.texto}>{etiqueta}</span>
    </div>
  );
}
