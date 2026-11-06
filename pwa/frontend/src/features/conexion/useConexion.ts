import { useEffect, useState } from 'react';

interface Conexion {
  enLinea: boolean;
}

/** Estado de la conexión del navegador (eventos `online`/`offline`). */
export function useConexion(): Conexion {
  const [enLinea, setEnLinea] = useState(() => navigator.onLine);

  useEffect(() => {
    const alConectar = (): void => setEnLinea(true);
    const alDesconectar = (): void => setEnLinea(false);

    window.addEventListener('online', alConectar);
    window.addEventListener('offline', alDesconectar);

    return () => {
      window.removeEventListener('online', alConectar);
      window.removeEventListener('offline', alDesconectar);
    };
  }, []);

  return { enLinea };
}
