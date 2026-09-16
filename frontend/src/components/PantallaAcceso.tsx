import type { ReactNode } from 'react'

/** Diseño compartido de las pantallas públicas (login y registro de prueba). */
export function PantallaAcceso({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-dvh flex-col bg-slate-900 lg:flex-row">
      <div className="flex flex-col justify-center px-6 pt-10 pb-6 sm:px-10 lg:sticky lg:top-0 lg:h-dvh lg:w-1/2 lg:px-16">
        <div className="flex items-center gap-3">
          <svg viewBox="0 0 32 32" className="size-10" aria-hidden>
            <rect width="32" height="32" rx="7" fill="#f97316" />
            <path d="M13 7h6v6h6v6h-6v6h-6v-6H7v-6h6z" fill="#fff" />
          </svg>
          <span className="text-2xl font-bold text-white">RescueSync</span>
        </div>
        <h1 className="mt-6 max-w-md text-2xl font-semibold leading-tight text-white sm:text-3xl lg:text-4xl">
          Coordinación regional de la respuesta ante desastres.
        </h1>
        <p className="mt-3 max-w-md text-slate-400">
          Municipios, Centro Coordinador y ONGs trabajando sobre la misma emergencia, desde el registro hasta el cierre.
        </p>
      </div>
      <div className="flex flex-1 items-start justify-center px-4 pb-10 sm:px-6 lg:items-center lg:bg-slate-100 lg:py-10">
        {children}
      </div>
    </div>
  )
}
