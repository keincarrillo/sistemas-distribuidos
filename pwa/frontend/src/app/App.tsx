import BotonInstalar from '@/features/conexion/BotonInstalar';
import ConexionBanner from '@/features/conexion/ConexionBanner';
import { useConexion } from '@/features/conexion/useConexion';
import EstadoPanel from '@/features/estado/EstadoPanel';
import PedidoForm from '@/features/pedidos/PedidoForm';
import styles from './App.module.scss';

/** Contenedor raíz: composición de las dos secciones y aviso de conexión. */
export default function App() {
  const { enLinea } = useConexion();

  return (
    <div className={styles.app}>
      {!enLinea && <ConexionBanner />}
      <header className={styles.app__header}>
        <div>
          <h1 className={styles.app__titulo}>Tienda SD</h1>
          <p className={styles.app__subtitulo}>Tienda en línea como servicio web REST</p>
        </div>
        <BotonInstalar />
      </header>
      <main className={styles.app__main}>
        <PedidoForm />
        <EstadoPanel />
      </main>
      <footer className={styles.app__footer}>
        <p>Práctica de Sistemas Distribuidos — React + Spring Boot como PWA</p>
      </footer>
    </div>
  );
}
