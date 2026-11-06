import { RefreshCw } from 'lucide-react';
import { Boton, EstadoPill, Metrica, Spinner, Tarjeta } from '@/components';
import styles from './EstadoPanel.module.scss';
import { useEstado } from './useEstado';

/** Panel de monitorización del back (container): cola, almacén, peticiones… */
export default function EstadoPanel() {
  const { datos, salud, cargando, error, recargar } = useEstado();

  return (
    <Tarjeta titulo="Estado del servicio" accion={<IndicadorSalud salud={salud} error={error} />}>
      {cargando && !datos && <Spinner etiqueta="Consultando el estado…" />}

      {error && !datos && (
        <div className={styles.panel__aviso} role="alert">
          <p>{error}</p>
          <Boton variante="secundario" icono={<RefreshCw size={16} />} onClick={recargar}>
            Reintentar
          </Boton>
        </div>
      )}

      {datos && (
        <>
          <dl className={styles.panel__metricas}>
            <Metrica etiqueta="Cola" valor={datos.colaActual} sufijo={`/ ${datos.capacidadCola}`} />
            <Metrica
              etiqueta="Almacén"
              valor={datos.almacenDisponible}
              sufijo={`/ ${datos.capacidadAlmacen}`}
            />
            <Metrica etiqueta="Activas" valor={datos.peticionesActivas} />
            <Metrica etiqueta="Atendidas" valor={datos.peticionesAtendidas} />
            <Metrica etiqueta="Rechazadas" valor={datos.peticionesRechazadas} />
            <Metrica etiqueta="Procesados" valor={datos.pedidosProcesados} />
            <Metrica etiqueta="Workers" valor={datos.workers} />
          </dl>
          {error && <p className={styles.panel__nota}>Última consulta fallida: {error}</p>}
        </>
      )}
    </Tarjeta>
  );
}

function IndicadorSalud({ salud, error }: { salud: string | null; error: string | null }) {
  if (error) {
    return <EstadoPill tono="error">Sin conexión</EstadoPill>;
  }
  if (!salud) {
    return null;
  }
  return <EstadoPill tono={salud === 'UP' ? 'ok' : 'aviso'}>{`Health: ${salud}`}</EstadoPill>;
}
