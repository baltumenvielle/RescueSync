import type { ReactNode } from 'react'
import type { Lote } from '../api/types'
import { fmtNumero, PRIORIDADES, TIPOS_LOTE } from '../lib/formato'
import { Etiqueta } from './ui'

/** Lista de lotes: tabla en pantallas anchas, tarjetas en móvil. */
export function TablaLotes({ lotes, acciones }: { lotes: Lote[]; acciones?: (lote: Lote) => ReactNode }) {
  return (
    <>
      <ul className="grid gap-2 md:hidden">
        {lotes.map((l) => (
          <li key={l.id} className="rounded-lg bg-slate-50 p-3 ring-1 ring-slate-200">
            <div className="flex items-start justify-between gap-2">
              <p className="font-medium text-slate-900">{l.descripcion}</p>
              <Etiqueta tono={l.prioridad === 'ALTA' ? 'rojo' : l.prioridad === 'MEDIA' ? 'ambar' : 'gris'}>{PRIORIDADES[l.prioridad]}</Etiqueta>
            </div>
            <p className="mt-1 text-sm text-slate-600">
              {fmtNumero(l.cantidadRequerida)} {l.unidad} · {TIPOS_LOTE[l.tipo]}
            </p>
            {acciones && <div className="mt-2 flex gap-2">{acciones(l)}</div>}
          </li>
        ))}
      </ul>
      <div className="hidden overflow-x-auto md:block">
        <table className="w-full text-left text-sm">
          <thead className="text-xs uppercase tracking-wide text-slate-500">
            <tr>
              <th className="py-2 pr-4 font-medium">Descripción</th>
              <th className="py-2 pr-4 font-medium">Tipo</th>
              <th className="py-2 pr-4 text-right font-medium">Cantidad</th>
              <th className="py-2 pr-4 font-medium">Prioridad</th>
              {acciones && <th className="py-2 font-medium"><span className="sr-only">Acciones</span></th>}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {lotes.map((l) => (
              <tr key={l.id}>
                <td className="py-2.5 pr-4 font-medium text-slate-900">{l.descripcion}</td>
                <td className="py-2.5 pr-4 text-slate-600">{TIPOS_LOTE[l.tipo]}</td>
                <td className="py-2.5 pr-4 text-right tabular-nums">
                  {fmtNumero(l.cantidadRequerida)} <span className="text-slate-500">{l.unidad}</span>
                </td>
                <td className="py-2.5 pr-4">
                  <Etiqueta tono={l.prioridad === 'ALTA' ? 'rojo' : l.prioridad === 'MEDIA' ? 'ambar' : 'gris'}>{PRIORIDADES[l.prioridad]}</Etiqueta>
                </td>
                {acciones && <td className="py-2.5 text-right whitespace-nowrap">{acciones(l)}</td>}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  )
}
