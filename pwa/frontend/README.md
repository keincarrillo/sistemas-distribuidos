# frontend — PWA de la tienda (React + TypeScript + Vite + Bun)

Interfaz instalable de la tienda REST (`../back/`). Es una SPA compilada por
Vite hacia `../back/src/main/resources/static/`, donde Spring Boot la sirve en
`/` (mismo origen: sin CORS). En desarrollo, Vite corre en `:5173` con proxy de
`/api` y `/health` hacia `:8080`.

## Comandos

Se lanzan desde `pwa/` (Makefile orquestador) o desde aquí:

| `pwa/`               | `frontend/`      | Qué hace                                       |
| -------------------- | ---------------- | ---------------------------------------------- |
| `make web-install`   | `make install`   | `bun install`                                  |
| `make web`           | `make build`     | `tsc -b && vite build` → `../back/.../static/` |
| `make web-dev`       | `make dev`       | Vite en `:5173 con proxy al back`              |
| `make web-lint`      | `make lint`      | ESLint                                         |
| `make web-typecheck` | `make typecheck` | `tsc -b`                                       |
| `make web-format`    | `make format`    | Prettier (`--write`)                           |
| `make web-check`     | `make check`     | `format:check + lint + typecheck`              |

Scripts equivalentes en `package.json`: `dev`, `build`, `lint`, `typecheck`,
`format`, `format:check`, `preview`.

## Estructura

```
src/
├── main.tsx            # bootstrap: React + registro del service worker
├── app/                # ensamblaje: App (layout) y ErrorBoundary
├── lib/                # infra sin dominio: http (fachada fetch) y errores
├── components/         # UI genérica reutilizable + barrel (index.ts)
│   ├── Boton/  Campo/  Tarjeta/  EstadoPill/  Metrica/  Spinner/
├── features/           # un dominio por carpeta (espejo de la capa api/ del back)
│   ├── pedidos/        # types (DTOs), api, useCrearPedido, PedidoForm, Resultado
│   ├── estado/         # types, api, useEstado (polling), EstadoPanel
│   └── conexion/       # useConexion, useInstalacion, banner, botón instalar
├── hooks/              # hooks genéricos (useInterval)
├── styles/             # design system SCSS (arquitectura 7-1)
│   ├── abstracts/      # variables + mixins (inyectados en cada .scss de entrada)
│   ├── base/           # reset, tipografía y estilos globales
│   └── themes/         # variables CSS a nivel de tema
└── types/              # ampliaciones de tipos (beforeinstallprompt)
```

## Patrones de diseño

| Patrón                   | Dónde                                                          |
| ------------------------ | -------------------------------------------------------------- |
| Fachada                  | `lib/http.ts` + `features/*/api.ts`: nada llama a `fetch` solo |
| DTOs / adapter           | `features/*/types.ts` espejea los records del back             |
| Custom hooks             | `useCrearPedido`, `useEstado`, `useConexion`, `useInstalacion` |
| Container/presentational | contenedores con lógica vs componentes puros de props          |
| Strategy (variantes)     | `Boton variante=`, `EstadoPill tono=`                          |
| Observer                 | eventos `online/offline` y `beforeinstallprompt`               |
| Error Boundary           | `app/ErrorBoundary.tsx`                                        |
| Barrel exports           | `components/index.ts`                                          |

## Estilos (SCSS)

- Arquitectura **7-1** en `styles/` con `@use` (nunca `@import`).
- Los abstracts (variables y mixins) se **inyectan automáticamente** en cada
  `.scss` de entrada (`additionalData` en `vite.config.ts`); los parciales
  internos hacen `@use '../abstracts' as *;` a mano.
- **BEM** dentro de los `*.module.scss` (el módulo ya da scope: `.boton`,
  `.boton--secundario`, `.tarjeta__cabecera`).
- Mobile-first con `respond-to('md')`, tamaños fluidos con `clamp()`, targets
  táctiles ≥ 44px y foco visible global.

## PWA

- `vite-plugin-pwa` (Workbox): **app shell** precacheado (html/js/css/iconos)
  y `navigateFallback` → la app abre sin conexión.
- Manifest instalable (iconos 192/512 + maskable en `public/icons/`).
- `/api` y `/health` **nunca** se cachean: si la API no responde, la UI lo dice
  (banner de sin conexión y estados de error).

## Reglas de calidad

- TypeScript **strict**, sin `any` (`@typescript-eslint/no-explicit-any`).
- Archivos cortos: `max-lines` 250 y `max-lines-per-function` 80 (warnings).
- Prettier con los valores por defecto del proyecto (`.prettierrc`).
- Alias de import: `@/*` → `src/*` (configurado en `vite.config.ts` y
  `tsconfig.app.json`).
