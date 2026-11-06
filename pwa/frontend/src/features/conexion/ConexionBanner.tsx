import { WifiOff } from 'lucide-react';
import styles from './conexion.module.scss';

/** Aviso cuando el navegador pierde la conexión (la UI sale del cache). */
export default function ConexionBanner() {
  return (
    <div className={styles.banner} role="status" aria-live="polite">
      <WifiOff size={16} aria-hidden="true" />
      <span>Sin conexión: la app muestra la versión cacheada</span>
    </div>
  );
}
