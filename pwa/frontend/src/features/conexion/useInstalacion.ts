import { useCallback, useEffect, useState } from 'react';

interface Instalacion {
  puedeInstalar: boolean;
  instalar: () => Promise<void>;
}

/**
 * Expone el evento `beforeinstallprompt` (Observer del navegador):
 * `puedeInstalar` habilita el botón de instalación de la PWA.
 */
export function useInstalacion(): Instalacion {
  const [evento, setEvento] = useState<BeforeInstallPromptEvent | null>(null);
  const [instalada, setInstalada] = useState(false);

  useEffect(() => {
    const alOfferInstalar = (eventoPwa: BeforeInstallPromptEvent): void => {
      eventoPwa.preventDefault();
      setEvento(eventoPwa);
    };
    const alInstalar = (): void => setInstalada(true);

    window.addEventListener('beforeinstallprompt', alOfferInstalar);
    window.addEventListener('appinstalled', alInstalar);

    return () => {
      window.removeEventListener('beforeinstallprompt', alOfferInstalar);
      window.removeEventListener('appinstalled', alInstalar);
    };
  }, []);

  const instalar = useCallback(async (): Promise<void> => {
    if (!evento) {
      return;
    }
    await evento.prompt();
    const { outcome } = await evento.userChoice;
    if (outcome === 'accepted') {
      setInstalada(true);
    }
    setEvento(null);
  }, [evento]);

  return { puedeInstalar: evento !== null && !instalada, instalar };
}
