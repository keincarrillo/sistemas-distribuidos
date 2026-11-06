import { useState, type FormEvent } from 'react';
import { RotateCcw, Send } from 'lucide-react';
import { Boton, Campo, CampoSelect, EstadoPill, Spinner, Tarjeta } from '@/components';
import ResultadoPedido from './ResultadoPedido';
import styles from './PedidoForm.module.scss';
import { CATALOGO } from './types';
import { useCrearPedido } from './useCrearPedido';

/** Contenedor del formulario: valida en cliente, envía y pinta el resultado. */
export default function PedidoForm() {
  const { estado, enviar, reiniciar } = useCrearPedido();
  const [producto, setProducto] = useState<string>(CATALOGO[0]);
  const [cliente, setCliente] = useState('');
  const [errorCliente, setErrorCliente] = useState<string | null>(null);

  const enviando = estado.fase === 'enviando';

  const enviarPedido = (evento: FormEvent<HTMLFormElement>): void => {
    evento.preventDefault();
    const clienteLimpio = cliente.trim();
    if (clienteLimpio.length < 2) {
      setErrorCliente('Escribe un nombre de cliente (mínimo 2 caracteres)');
      return;
    }
    setErrorCliente(null);
    void enviar({ producto, cliente: clienteLimpio });
  };

  const otroPedido = (): void => {
    reiniciar();
    setCliente('');
    setProducto(CATALOGO[0]);
  };

  return (
    <Tarjeta titulo="Nuevo pedido">
      <form className={styles.formulario} onSubmit={enviarPedido} noValidate>
        <CampoSelect
          etiqueta="Producto"
          opciones={CATALOGO}
          value={producto}
          onChange={(evento) => setProducto(evento.target.value)}
          disabled={enviando}
        />
        <Campo
          etiqueta="Cliente"
          placeholder="Nombre del cliente"
          value={cliente}
          onChange={(evento) => setCliente(evento.target.value)}
          error={errorCliente ?? undefined}
          disabled={enviando}
          autoComplete="name"
        />
        <div className={styles.formulario__acciones}>
          <Boton type="submit" disabled={enviando} icono={<Send size={16} />}>
            {enviando ? 'Enviando…' : 'Enviar pedido'}
          </Boton>
        </div>
      </form>

      {enviando && <Spinner etiqueta="Esperando a un worker…" />}

      {estado.fase === 'error' && (
        <div className={styles.formulario__error} role="alert">
          <EstadoPill tono="error">
            {estado.codigo ? `HTTP ${estado.codigo}` : 'Sin conexión'}
          </EstadoPill>
          <p className={styles.formulario__mensaje}>{estado.mensaje}</p>
          <Boton variante="secundario" icono={<RotateCcw size={16} />} onClick={reiniciar}>
            Reintentar
          </Boton>
        </div>
      )}

      {estado.fase === 'ok' && (
        <ResultadoPedido resultado={estado.resultado} onOtroPedido={otroPedido} />
      )}
    </Tarjeta>
  );
}
