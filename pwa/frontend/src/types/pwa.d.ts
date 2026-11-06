/** Evento `beforeinstallprompt` (todavía no está en los tipos oficiales de TS). */
interface BeforeInstallPromptEvent extends Event {
  prompt(): Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed'; platform: string }>;
}

interface WindowEventMap {
  beforeinstallprompt: BeforeInstallPromptEvent;
}
