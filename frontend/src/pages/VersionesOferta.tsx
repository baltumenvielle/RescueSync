import { useQuery } from '@tanstack/react-query'
import { useParams } from 'react-router'
import { mensajeDeError } from '../api/client'
import { api } from '../api/endpoints'
import type { VersionOferta } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { EstadoOfertaBadge } from '../components/badges'
import { Alerta, Cargando, Encabezado } from '../components/ui'
import { cx } from '../lib/cx'
import { fmtFecha, fmtNumero, TIPOS_DESASTRE } from '../lib/formato'

function diferencia(anterior: VersionOferta | undefined, loteId: number, cantidad: number) {
  if (!anterior) return null
  const previa = anterior.items.find((i) => i.loteId === loteId)?.cantidadOfrecida
  if (previa === undefined) return <span className="text-xs font-medium text-emerald-700">nuevo</span>
  if (previa === cantidad) return null
  const delta = cantidad - previa
  return <span className={cx('text-xs font-medium', delta > 0 ? 'text-emerald-700' : 'text-red-700')}>{delta > 0 ? `+${fmtNumero(delta)}` : fmtNumero(delta)}</span>
}

export function VersionesOferta() {
  const id = Number(useParams().id)
  const { usuario } = useAuth()
  const oferta = useQuery({ queryKey: ['oferta', id], queryFn: () => api.oferta(id) })
  const versiones = useQuery({ queryKey: ['versiones', id], queryFn: () => api.versiones(id) })

  if (oferta.isPending || versiones.isPending) return <Cargando />
  if (!oferta.data || !versiones.data) return <Alerta>{mensajeDeError(oferta.error ?? versiones.error)}</Alerta>
  const o = oferta.data
  const lista = [...versiones.data].reverse()

  return (
    <>
      <Encabezado
        titulo="Historial de versiones"
        subtitulo={`${o.ong.nombre} · Emergencia #${o.emergencia.id} (${TIPOS_DESASTRE[o.emergencia.tipoDesastre]})`}
        volver={usuario?.rol === 'REPRESENTANTE_ONG' ? { to: '/ong/ofertas', texto: 'Mis ofertas' } : { to: `/emergencias/${o.emergencia.id}`, texto: 'Emergencia' }}
        acciones={<EstadoOfertaBadge estado={o.estado} />}
      />
      <ol className="relative grid gap-4 border-l-2 border-slate-200 pl-5 sm:pl-6">
        {lista.map((v) => {
          const anterior = versiones.data.find((x) => x.numero === v.numero - 1)
          const retirados = anterior?.items.filter((i) => !v.items.some((x) => x.loteId === i.loteId)) ?? []
          const vigente = v.numero === o.versionActual
          return (
            <li key={v.numero} className="relative">
              <span className={cx('absolute top-4 -left-[29px] size-3.5 rounded-full ring-4 ring-slate-100 sm:-left-[33px]', vigente ? 'bg-marca-500' : 'bg-slate-400')} aria-hidden />
              <div className={cx('rounded-xl bg-white p-4 shadow-sm ring-1', vigente ? 'ring-marca-500/50' : 'ring-slate-200')}>
                <div className="flex flex-wrap items-baseline justify-between gap-2">
                  <p className="font-semibold text-slate-900">
                    Versión {v.numero} {vigente && <span className="ml-1 text-xs font-medium text-marca-700">(vigente)</span>}
                  </p>
                  <p className="text-xs text-slate-500">
                    {fmtFecha(v.createdAt)} · {v.autor}
                  </p>
                </div>
                {v.comentario && <p className="mt-1 text-sm italic text-slate-600">“{v.comentario}”</p>}
                <ul className="mt-3 grid gap-1.5 text-sm">
                  {v.items.map((i) => (
                    <li key={i.loteId} className="flex flex-wrap items-baseline justify-between gap-2">
                      <span className="text-slate-700">{i.descripcionLote}</span>
                      <span className="flex items-baseline gap-2 tabular-nums">
                        {diferencia(anterior, i.loteId, i.cantidadOfrecida)}
                        <span className="font-medium text-slate-900">
                          {fmtNumero(i.cantidadOfrecida)} {i.unidad}
                        </span>
                      </span>
                    </li>
                  ))}
                  {retirados.map((i) => (
                    <li key={`r-${i.loteId}`} className="flex justify-between gap-2 text-slate-400 line-through">
                      <span>{i.descripcionLote}</span>
                      <span>
                        {fmtNumero(i.cantidadOfrecida)} {i.unidad}
                      </span>
                    </li>
                  ))}
                </ul>
              </div>
            </li>
          )
        })}
      </ol>
    </>
  )
}
