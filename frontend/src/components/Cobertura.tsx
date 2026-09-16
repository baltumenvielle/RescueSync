import type { Cobertura } from '../api/types'
import { fmtNumero, TIPOS_LOTE } from '../lib/formato'
import { Barra, Etiqueta } from './ui'

export function TablaCobertura({ cobertura }: { cobertura: Cobertura }) {
  return (
    <div>
      <div className="mb-4 flex flex-wrap items-center gap-2">
        <Etiqueta tono={cobertura.completa ? 'verde' : 'ambar'}>
          {cobertura.completa ? 'Cobertura completa' : 'Cobertura insuficiente'}
        </Etiqueta>
        <span className="text-sm text-slate-600">
          {cobertura.lotesCubiertos} de {cobertura.lotesTotales} lotes cubiertos
        </span>
      </div>
      <ul className="grid gap-4">
        {cobertura.lotes.map((l) => (
          <li key={l.loteId}>
            <div className="mb-1 flex flex-wrap items-baseline justify-between gap-x-4 gap-y-1">
              <p className="text-sm font-medium text-slate-900">
                {l.descripcion} <span className="font-normal text-slate-500">· {TIPOS_LOTE[l.tipo]}</span>
              </p>
              <p className="text-sm tabular-nums text-slate-700">
                {fmtNumero(l.cantidadOfrecida)} / {fmtNumero(l.cantidadRequerida)} {l.unidad}
                <span className="ml-2 text-xs text-slate-500">
                  ({l.ofertasQueAportan} {l.ofertasQueAportan === 1 ? 'oferta' : 'ofertas'})
                </span>
              </p>
            </div>
            <Barra porcentaje={l.porcentaje} completo={l.cubierto} />
          </li>
        ))}
      </ul>
      <p className="mt-4 text-xs text-slate-500">Se consideran las ofertas enviadas, en su versión vigente.</p>
    </div>
  )
}
