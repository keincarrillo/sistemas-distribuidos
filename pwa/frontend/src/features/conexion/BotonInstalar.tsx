import { Download } from 'lucide-react';
import { Boton } from '@/components';
import { useInstalacion } from './useInstalacion';

/** Botón "Instalar app": solo aparece si el navegador ofrece `beforeinstallprompt`. */
export default function BotonInstalar() {
  const { puedeInstalar, instalar } = useInstalacion();

  if (!puedeInstalar) {
    return null;
  }

  return (
    <Boton variante="secundario" icono={<Download size={16} />} onClick={() => void instalar()}>
      Instalar app
    </Boton>
  );
}
