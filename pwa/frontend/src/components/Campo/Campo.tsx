import { useId, type InputHTMLAttributes } from 'react';
import styles from './campo.module.scss';

interface CampoProps extends InputHTMLAttributes<HTMLInputElement> {
  etiqueta: string;
  error?: string;
}

/** Campo de formulario reutilizable: etiqueta + input + error accesible (aria). */
export default function Campo({ etiqueta, error, id, className, ...resto }: CampoProps) {
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
      <input
        id={idCampo}
        className={control}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? idError : undefined}
        {...resto}
      />
      {error && (
        <p className={styles.campo__error} id={idError}>
          {error}
        </p>
      )}
    </div>
  );
}
