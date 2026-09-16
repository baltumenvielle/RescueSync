import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { mensajeDeError } from '../api/client'
import { api } from '../api/endpoints'
import type { Tarea } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { EstadoEmergenciaBadge, GravedadBadge } from '../components/badges'
import { Alerta, Boton, BotonLink, Cargando, Encabezado, Vacio } from '../components/ui'
import { ACCIONES_TAREA, fmtFecha, TIPOS_DESASTRE } from '../lib/formato'

function destino(t: Tarea): string {
  const id = t.emergenciaId
  if (id == null) return '/'
  switch (t.accion) {
    case 'REVISION':
      return `/emergencias/${id}/revision`
    case 'DESGLOSE':
      return `/emergencias/${id}/desglose`
    case 'EVALUAR_COBERTURA':
      return `/emergencias/${id}/decision`
    default:
      return `/emergencias/${id}`
  }
}

export function Bandeja() {
  const { usuario } = useAuth()
  const tareas = useQuery({ queryKey: ['tareas'], queryFn: api.tareas, refetchInterval: 15_000 })

  return (
    <>
      <Encabezado
        titulo="Bandeja de tareas"
        subtitulo="Tareas del proceso que esperan una acción de su parte."
        acciones={
          <Boton variante="secundario" onClick={() => tareas.refetch()} cargando={tareas.isFetching}>
            Actualizar
          </Boton>
        }
      />
      {tareas.isPending && <Cargando />}
      {tareas.isError && <Alerta>{mensajeDeError(tareas.error)}</Alerta>}
      {tareas.data?.length === 0 && (
        <Vacio titulo="No tiene tareas pendientes">
          {usuario?.rol === 'REPRESENTANTE_ONG' ? (
            <>
              Las ofertas se cargan desde <Link className="font-medium text-marca-700 underline" to="/ong/convocatorias">Convocatorias abiertas</Link>.
            </>
          ) : (
            'Cuando el proceso requiera su intervención, la tarea aparecerá aquí.'
          )}
        </Vacio>
      )}
      {tareas.data && tareas.data.length > 0 && (
        <ul className="grid gap-3">
          {tareas.data.map((t) => (
            <li key={t.taskId} className="flex flex-col gap-3 rounded-xl bg-white p-4 shadow-sm ring-1 ring-slate-200 sm:flex-row sm:items-center sm:justify-between sm:p-5">
              <div className="min-w-0">
                <p className="text-xs font-medium uppercase tracking-wide text-marca-700">{t.nombreTarea}</p>
                {t.emergencia ? (
                  <>
                    <p className="mt-1 font-semibold text-slate-900">
                      #{t.emergencia.id} · {TIPOS_DESASTRE[t.emergencia.tipoDesastre]} en {t.emergencia.zonaAfectada}
                    </p>
                    <div className="mt-2 flex flex-wrap items-center gap-2 text-sm text-slate-500">
                      <GravedadBadge gravedad={t.emergencia.gravedad} />
                      <EstadoEmergenciaBadge estado={t.emergencia.estado} />
                      <span>{t.emergencia.municipio.nombre}</span>
                      <span aria-hidden>·</span>
                      <span>Disponible desde {fmtFecha(t.fechaDisponible)}</span>
                    </div>
                  </>
                ) : (
                  <p className="mt-1 text-sm text-slate-500">Caso {t.caseId}</p>
                )}
              </div>
              <BotonLink to={destino(t)} className="w-full shrink-0 sm:w-auto">
                {ACCIONES_TAREA[t.accion]}
              </BotonLink>
            </li>
          ))}
        </ul>
      )}
    </>
  )
}
