import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router'
import { mensajeDeError } from '../api/client'
import { api } from '../api/endpoints'
import type { EstadoEmergencia } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { EstadoEmergenciaBadge, GravedadBadge } from '../components/badges'
import { Alerta, BotonLink, Cargando, Encabezado, Selector, Vacio } from '../components/ui'
import { ESTADOS_EMERGENCIA, fmtFecha, opciones, TIPOS_DESASTRE } from '../lib/formato'

export function Emergencias() {
  const { usuario } = useAuth()
  const [estado, setEstado] = useState<EstadoEmergencia | ''>('')
  const emergencias = useQuery({
    queryKey: ['emergencias', estado],
    queryFn: () => api.emergencias(estado || undefined),
  })
  const esMunicipio = usuario?.rol === 'OPERADOR_MUNICIPAL'

  return (
    <>
      <Encabezado
        titulo={esMunicipio ? 'Mis emergencias' : 'Emergencias'}
        subtitulo={
          esMunicipio
            ? `Emergencias registradas por ${usuario?.organizacion.nombre}.`
            : usuario?.rol === 'AUDITOR'
              ? 'Vista de solo lectura sobre todas las emergencias de la red.'
              : 'Todas las emergencias de la región.'
        }
        acciones={esMunicipio && <BotonLink to="/emergencias/nueva">Registrar emergencia</BotonLink>}
      />
      <div className="mb-4 flex items-center gap-2">
        <label htmlFor="filtro-estado" className="text-sm font-medium text-slate-700">
          Estado
        </label>
        <Selector id="filtro-estado" className="max-w-xs" value={estado} onChange={(e) => setEstado(e.target.value as EstadoEmergencia | '')}>
          <option value="">Todos</option>
          {opciones(ESTADOS_EMERGENCIA).map((o) => (
            <option key={o.valor} value={o.valor}>
              {o.etiqueta}
            </option>
          ))}
        </Selector>
      </div>

      {emergencias.isPending && <Cargando />}
      {emergencias.isError && <Alerta>{mensajeDeError(emergencias.error)}</Alerta>}
      {emergencias.data?.length === 0 && <Vacio titulo="No hay emergencias para mostrar" />}
      {emergencias.data && emergencias.data.length > 0 && (
        <ul className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          {emergencias.data.map((e) => (
            <li key={e.id}>
              <Link
                to={`/emergencias/${e.id}`}
                className="flex h-full flex-col rounded-xl bg-white p-4 shadow-sm ring-1 ring-slate-200 transition hover:ring-marca-500 focus-visible:outline-2 focus-visible:outline-marca-600"
              >
                <div className="flex items-start justify-between gap-2">
                  <p className="font-semibold text-slate-900">
                    #{e.id} · {TIPOS_DESASTRE[e.tipoDesastre]}
                  </p>
                  <GravedadBadge gravedad={e.gravedad} />
                </div>
                <p className="mt-1 line-clamp-2 text-sm text-slate-600">{e.zonaAfectada}</p>
                <div className="mt-auto flex flex-wrap items-center justify-between gap-2 pt-4 text-xs text-slate-500">
                  <EstadoEmergenciaBadge estado={e.estado} />
                  <span>{esMunicipio ? fmtFecha(e.createdAt) : e.municipio.nombre}</span>
                </div>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </>
  )
}
