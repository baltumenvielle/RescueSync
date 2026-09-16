import type { ButtonHTMLAttributes, InputHTMLAttributes, ReactNode, SelectHTMLAttributes, TextareaHTMLAttributes } from 'react'
import { useId } from 'react'
import { Link } from 'react-router'
import { cx } from '../lib/cx'


type Variante = 'primario' | 'secundario' | 'peligro' | 'fantasma'

const VARIANTES: Record<Variante, string> = {
  primario: 'bg-marca-600 text-white hover:bg-marca-700 focus-visible:outline-marca-600',
  secundario: 'bg-white text-slate-800 ring-1 ring-inset ring-slate-300 hover:bg-slate-50 focus-visible:outline-slate-500',
  peligro: 'bg-white text-red-700 ring-1 ring-inset ring-red-200 hover:bg-red-50 focus-visible:outline-red-600',
  fantasma: 'text-slate-700 hover:bg-slate-200/70 focus-visible:outline-slate-500',
}

const BASE_BOTON =
  'inline-flex min-h-10 items-center justify-center gap-2 rounded-lg px-4 py-2 text-sm font-semibold transition focus-visible:outline-2 focus-visible:outline-offset-2 disabled:cursor-not-allowed disabled:opacity-50'

export function Boton({
  variante = 'primario',
  cargando,
  className,
  children,
  disabled,
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & { variante?: Variante; cargando?: boolean }) {
  return (
    <button className={cx(BASE_BOTON, VARIANTES[variante], className)} disabled={disabled || cargando} {...props}>
      {cargando && <Spinner className="size-4" />}
      {children}
    </button>
  )
}

export function BotonLink({ to, variante = 'primario', className, children }: { to: string; variante?: Variante; className?: string; children: ReactNode }) {
  return (
    <Link to={to} className={cx(BASE_BOTON, VARIANTES[variante], className)}>
      {children}
    </Link>
  )
}

export function Spinner({ className }: { className?: string }) {
  return (
    <svg className={cx('animate-spin', className ?? 'size-5')} viewBox="0 0 24 24" fill="none" aria-hidden>
      <circle cx="12" cy="12" r="10" stroke="currentColor" strokeOpacity="0.25" strokeWidth="4" />
      <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor" strokeWidth="4" strokeLinecap="round" />
    </svg>
  )
}

export function Tarjeta({ titulo, acciones, children, className }: { titulo?: ReactNode; acciones?: ReactNode; children: ReactNode; className?: string }) {
  return (
    <section className={cx('rounded-xl bg-white shadow-sm ring-1 ring-slate-200', className)}>
      {(titulo || acciones) && (
        <header className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 px-4 py-3 sm:px-5">
          {titulo && <h2 className="text-base font-semibold text-slate-900">{titulo}</h2>}
          {acciones && <div className="flex flex-wrap gap-2">{acciones}</div>}
        </header>
      )}
      <div className="p-4 sm:p-5">{children}</div>
    </section>
  )
}

export function Encabezado({ titulo, subtitulo, acciones, volver }: { titulo: ReactNode; subtitulo?: ReactNode; acciones?: ReactNode; volver?: { to: string; texto: string } }) {
  return (
    <div className="mb-5 flex flex-col gap-3 sm:mb-6 md:flex-row md:items-end md:justify-between">
      <div className="min-w-0">
        {volver && (
          <Link to={volver.to} className="mb-1 inline-block text-sm font-medium text-slate-500 hover:text-slate-800">
            ← {volver.texto}
          </Link>
        )}
        <h1 className="text-xl font-bold tracking-tight text-slate-900 sm:text-2xl">{titulo}</h1>
        {subtitulo && <p className="mt-1 text-sm text-slate-600">{subtitulo}</p>}
      </div>
      {acciones && <div className="flex flex-wrap gap-2">{acciones}</div>}
    </div>
  )
}

interface CampoProps {
  etiqueta: string
  error?: string
  ayuda?: string
  children: (id: string, describedBy?: string) => ReactNode
  className?: string
}

export function Campo({ etiqueta, error, ayuda, children, className }: CampoProps) {
  const id = useId()
  const descId = error || ayuda ? `${id}-desc` : undefined
  return (
    <div className={className}>
      <label htmlFor={id} className="mb-1 block text-sm font-medium text-slate-700">
        {etiqueta}
      </label>
      {children(id, descId)}
      {error ? (
        <p id={descId} className="mt-1 text-sm text-red-600">
          {error}
        </p>
      ) : (
        ayuda && (
          <p id={descId} className="mt-1 text-xs text-slate-500">
            {ayuda}
          </p>
        )
      )}
    </div>
  )
}

const BASE_CONTROL =
  'block w-full rounded-lg border-0 bg-white px-3 py-2 text-base text-slate-900 shadow-xs ring-1 ring-inset placeholder:text-slate-400 focus:ring-2 focus:ring-inset focus:ring-marca-500 disabled:bg-slate-50 sm:text-sm'

export function Entrada({ invalido, className, ...props }: InputHTMLAttributes<HTMLInputElement> & { invalido?: boolean }) {
  return <input className={cx(BASE_CONTROL, 'min-h-10', invalido ? 'ring-red-400' : 'ring-slate-300', className)} aria-invalid={invalido || undefined} {...props} />
}

export function AreaTexto({ invalido, className, ...props }: TextareaHTMLAttributes<HTMLTextAreaElement> & { invalido?: boolean }) {
  return <textarea className={cx(BASE_CONTROL, invalido ? 'ring-red-400' : 'ring-slate-300', className)} aria-invalid={invalido || undefined} {...props} />
}

export function Selector({ invalido, className, children, ...props }: SelectHTMLAttributes<HTMLSelectElement> & { invalido?: boolean }) {
  return (
    <select className={cx(BASE_CONTROL, 'min-h-10', invalido ? 'ring-red-400' : 'ring-slate-300', className)} aria-invalid={invalido || undefined} {...props}>
      {children}
    </select>
  )
}

type Tono = 'gris' | 'azul' | 'verde' | 'ambar' | 'rojo' | 'naranja' | 'violeta'

const TONOS: Record<Tono, string> = {
  gris: 'bg-slate-100 text-slate-700 ring-slate-500/20',
  azul: 'bg-sky-50 text-sky-800 ring-sky-600/20',
  verde: 'bg-emerald-50 text-emerald-800 ring-emerald-600/20',
  ambar: 'bg-amber-50 text-amber-800 ring-amber-600/25',
  rojo: 'bg-red-50 text-red-800 ring-red-600/20',
  naranja: 'bg-marca-50 text-marca-700 ring-marca-600/25',
  violeta: 'bg-violet-50 text-violet-800 ring-violet-600/20',
}

export function Etiqueta({ tono = 'gris', children }: { tono?: Tono; children: ReactNode }) {
  return <span className={cx('inline-flex items-center whitespace-nowrap rounded-md px-2 py-0.5 text-xs font-medium ring-1 ring-inset', TONOS[tono])}>{children}</span>
}

export function Alerta({ tipo = 'error', titulo, children }: { tipo?: 'error' | 'info' | 'exito' | 'aviso'; titulo?: string; children?: ReactNode }) {
  const estilos = {
    error: 'bg-red-50 text-red-800 ring-red-200',
    info: 'bg-sky-50 text-sky-900 ring-sky-200',
    exito: 'bg-emerald-50 text-emerald-900 ring-emerald-200',
    aviso: 'bg-amber-50 text-amber-900 ring-amber-200',
  }[tipo]
  return (
    <div role={tipo === 'error' ? 'alert' : 'status'} className={cx('rounded-lg px-4 py-3 text-sm ring-1 ring-inset', estilos)}>
      {titulo && <p className="font-semibold">{titulo}</p>}
      {children && <div className={titulo ? 'mt-1' : undefined}>{children}</div>}
    </div>
  )
}

export function Cargando({ texto = 'Cargando…' }: { texto?: string }) {
  return (
    <div className="flex items-center justify-center gap-3 py-12 text-slate-500">
      <Spinner />
      <span className="text-sm">{texto}</span>
    </div>
  )
}

export function Vacio({ titulo, children }: { titulo: string; children?: ReactNode }) {
  return (
    <div className="rounded-xl border border-dashed border-slate-300 bg-white/60 px-6 py-10 text-center">
      <p className="font-medium text-slate-800">{titulo}</p>
      {children && <div className="mt-1 text-sm text-slate-500">{children}</div>}
    </div>
  )
}

export function Dato({ etiqueta, children }: { etiqueta: string; children: ReactNode }) {
  return (
    <div>
      <dt className="text-xs font-medium uppercase tracking-wide text-slate-500">{etiqueta}</dt>
      <dd className="mt-0.5 text-sm text-slate-900">{children}</dd>
    </div>
  )
}

export function Barra({ porcentaje, completo }: { porcentaje: number; completo: boolean }) {
  return (
    <div className="h-2 w-full overflow-hidden rounded-full bg-slate-200" role="progressbar" aria-valuenow={porcentaje} aria-valuemin={0} aria-valuemax={100}>
      <div className={cx('h-full rounded-full transition-all', completo ? 'bg-emerald-500' : 'bg-marca-500')} style={{ width: `${porcentaje}%` }} />
    </div>
  )
}
