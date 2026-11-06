import { useId, type SelectHTMLAttributes } from 'react';
import styles from './campo.module.scss';

interface CampoSelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  etiqueta: string;
  opciones: readonly string[];
  error?: string;
}

/** Select reutilizable con la misma estructura (y estilos) que `Campo`. */
export default function CampoSelect({
  etiqueta,
  opciones,
  error,
  id,
  className,
  ...resto
}: CampoSelectProps) {
  const idGenerado = useId();
  const idCampo = id ?? idGenerado;
  const idError = `${idCampo}-error`;
  const control = [styles.campo__control, error && styles['campo__control--invalido']]
    .filter(Boolean)
    .join(' ');
  const contenedor = [styles.campo, className].filter(Boolean).join(' ');

  return (
    <div className={contenedor}>
      <label className={styles.campo__etiqueta} htmlFor={idCampo}>
        {etiqueta}
      </label>
      <select
        id={idCampo}
        className={control}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? idError : undefined}
        {...resto}
      >
        {opciones.map((opcion) => (
          <option key={opcion} value={opcion}>
            {opcion}
          </option>
        ))}
      </select>
      {error && (
        <p className={styles.campo__error} id={idError}>
          {error}
        </p>
      )}
    </div>
  );
}
