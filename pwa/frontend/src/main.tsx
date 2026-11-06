import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { registerSW } from 'virtual:pwa-register';
import App from './app/App';
import ErrorBoundary from './app/ErrorBoundary';
import './styles/main.scss';

// Service worker: actualización automática (autoUpdate) y registro inmediato.
registerSW({ immediate: true });

const raiz = document.getElementById('root');

if (!raiz) {
  throw new Error('Falta el elemento #root en index.html');
}

createRoot(raiz).render(
  <StrictMode>
    <ErrorBoundary>
      <App />
    </ErrorBoundary>
  </StrictMode>,
);
