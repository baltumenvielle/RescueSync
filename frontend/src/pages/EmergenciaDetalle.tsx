import { useQuery } from '@tanstack/react-query'
import { Link, useLocation, useParams } from 'react-router'
import { mensajeDeError } from '../api/client'
import { api } from '../api/endpoints'
import { useAuth } from '../auth/AuthContext'
import { EstadoOfertaBadge } from '../components/badges'
import { TablaCobertura } from '../components/Cobertura'
import { CuentaRegresiva } from '../components/CuentaRegresiva'
import { ResumenEmergencia } from '../components/ResumenEmergencia'
import { SimuladorBpm } from '../components/SimuladorBpm'
import { TablaLotes } from '../components/TablaLotes'
import { Alerta, BotonLink, Cargando, Dato, Encabezado, Tarjeta, Vacio } from '../components/ui'
import { DECISIONES, fmtFecha, TIPOS_DESASTRE } from '../lib/formato'

export function EmergenciaDetalle() {
  const id = Number(useParams().id)
  const { usuario, tieneRol } = useAuth()
  const location = useLocation()
  const recienRegistrada = (location.state as { registrada?: boolean } | null)?.registrada

  const emergencia = useQuery({ queryKey: ['emergencia', id], queryFn: () => api.emergencia(id) })
  const veOfertas = tieneRol('CCR', 'AUDITOR')
  const e = emergencia.data
  const conConvocatoria = !!e?.fechaAperturaConvocatoria
  const cobertura = useQuery({
    queryKey: ['cobertura', id],
    queryFn: () => api.cobertura(id),
    enabled: veOfertas && conConvocatoria,
  })
  const ofertas = useQuery({
    queryKey: ['ofertas-emergencia', id],
    queryFn: () => api.ofertasDeEmergencia(id),
    enabled: veOfertas && conConvocatoria,
  })

  if (emergencia.isPending) return <Cargando />
  if (emergencia.isError || !e) return <Alerta>{mensajeDeError(emergencia.error)}</Alerta>

  const volver = usuario?.rol === 'REPRESENTANTE_ONG' ? { to: '/ong/convocatorias', texto: 'Convocatorias' } : { to: '/emergencias', texto: 'Emergencias' }

  return (
    <>
      <Encabezado
        titulo={`Emergencia #${e.id} · ${TIPOS_DESASTRE[e.tipoDesastre]}`}
        subtitulo={e.zonaAfectada}
        volver={volver}
        acciones={
          tieneRol('CCR') && (
            <>
              {e.estado === 'REGISTRADA' && <BotonLink to={`/emergencias/${e.id}/revision`}>Revisar información</BotonLink>}
              {e.estado === 'EN_REVISION' && <BotonLink to={`/emergencias/${e.id}/desglose`}>Definir lotes</BotonLink>}
              {e.estado === 'CONVOCATORIA_CERRADA' && <BotonLink to={`/emergencias/${e.id}/decision`}>Evaluar cobertura</BotonLink>}
            </>
          )
        }
      />
      <div className="grid gap-5">
        {recienRegistrada && (
          <Alerta tipo="exito" titulo="Emergencia registrada">
            Se inició el proceso de coordinación. El Centro Coordinador Regional revisará la información.
          </Alerta>
        )}
        {tieneRol('CCR') && <SimuladorBpm emergencia={e} />}
        <ResumenEmergencia emergencia={e} />

        {conConvocatoria && (
          <Tarjeta titulo="Convocatoria">
            <dl className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              <Dato etiqueta="Apertura">{fmtFecha(e.fechaAperturaConvocatoria)}</Dato>
              <Dato etiqueta="Cierre">{fmtFecha(e.fechaCierreConvocatoria)}</Dato>
              <Dato etiqueta="Tiempo restante">
                {e.estado === 'CONVOCATORIA_ABIERTA' && e.fechaCierreConvocatoria ? <CuentaRegresiva hasta={e.fechaCierreConvocatoria} /> : 'Cerrada'}
              </Dato>
              {e.decisionCcr && <Dato etiqueta="Última decisión del CCR">{DECISIONES[e.decisionCcr]}</Dato>}
            </dl>
          </Tarjeta>
        )}

        <Tarjeta titulo="Lotes de necesidades">
          {e.lotes.length === 0 ? (
            <Vacio titulo="Todavía no se definieron lotes">El Centro Coordinador desglosa la emergencia luego de revisarla.</Vacio>
          ) : (
            <TablaLotes lotes={e.lotes} />
          )}
        </Tarjeta>

        {veOfertas && conConvocatoria && (
          <div className="grid gap-5 lg:grid-cols-2">
            <Tarjeta titulo="Cobertura">
              {cobertura.isPending ? <Cargando /> : cobertura.data ? <TablaCobertura cobertura={cobertura.data} /> : <Alerta>{mensajeDeError(cobertura.error)}</Alerta>}
            </Tarjeta>
            <Tarjeta titulo="Ofertas recibidas">
              {ofertas.isPending && <Cargando />}
              {ofertas.data?.length === 0 && <p className="text-sm text-slate-500">Todavía no hay ofertas.</p>}
              <ul className="divide-y divide-slate-100">
                {ofertas.data?.map((o) => (
                  <li key={o.id} className="flex flex-wrap items-center justify-between gap-2 py-3">
                    <div>
                      <p className="font-medium text-slate-900">{o.ong.nombre}</p>
                      <p className="text-xs text-slate-500">
                        {o.vigente.items.length} {o.vigente.items.length === 1 ? 'lote' : 'lotes'} · versión {o.versionActual}
                      </p>
                    </div>
                    <div className="flex items-center gap-3">
                      <EstadoOfertaBadge estado={o.estado} />
                      <Link to={`/ofertas/${o.id}/versiones`} className="text-sm font-medium text-marca-700 hover:underline">
                        Historial
                      </Link>
                    </div>
                  </li>
                ))}
              </ul>
            </Tarjeta>
          </div>
        )}
      </div>
    </>
  )
}
