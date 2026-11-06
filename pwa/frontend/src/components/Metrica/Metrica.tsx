import styles from './Metrica.module.scss';

interface MetricaProps {
  etiqueta: string;
  valor: number | string;
  sufijo?: string;
}

/** Par etiqueta+valor para paneles de estadísticas (va dentro de un `<dl>`). */
export default function Metrica({ etiqueta, valor, sufijo }: MetricaProps) {
  return (
    <div className={styles.metrica}>
      <dt className={styles.metrica__etiqueta}>{etiqueta}</dt>
      <dd className={styles.metrica__valor}>
        {valor}
        {sufijo && <span className={styles.metrica__sufijo}> {sufijo}</span>}
      </dd>
    </div>
  );
}
