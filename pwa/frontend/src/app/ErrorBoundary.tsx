import { Component, type ErrorInfo, type ReactNode } from 'react';
import styles from './ErrorBoundary.module.scss';

interface Props {
  children: ReactNode;
}

interface State {
  error: Error | null;
}

/**
 * Patrón Error Boundary: si un render lanza, muestra un fallback
 * en lugar de dejar la pantalla en blanco.
 */
class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo): void {
    console.error('Error de render:', error, info.componentStack);
  }

  render(): ReactNode {
    const { error } = this.state;
    if (!error) {
      return this.props.children;
    }

    return (
      <div className={styles.contenedor} role="alert">
        <h1 className={styles.titulo}>Algo salió mal</h1>
        <p className={styles.texto}>La aplicación encontró un error inesperado.</p>
        <button type="button" className={styles.boton} onClick={() => window.location.reload()}>
          Recargar
        </button>
      </div>
    );
  }
}

export default ErrorBoundary;
