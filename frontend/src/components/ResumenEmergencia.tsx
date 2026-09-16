import type { EmergenciaDetalle } from '../api/types'
import { fmtFecha, TIPOS_DESASTRE } from '../lib/formato'
import { EstadoEmergenciaBadge, GravedadBadge } from './badges'
import { Dato, Tarjeta } from './ui'

export function ResumenEmergencia({ emergencia: e }: { emergencia: EmergenciaDetalle }) {
  return (
    <Tarjeta titulo="Información registrada por el municipio">
      <div className="mb-4 flex flex-wrap gap-2">
        <GravedadBadge gravedad={e.gravedad} />
        <EstadoEmergenciaBadge estado={e.estado} />
      </div>
      <dl className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <Dato etiqueta="Tipo de desastre">{TIPOS_DESASTRE[e.tipoDesastre]}</Dato>
        <Dato etiqueta="Zona afectada">{e.zonaAfectada}</Dato>
        <Dato etiqueta="Municipio">{e.municipio.nombre}</Dato>
        <Dato etiqueta="Registrada">{fmtFecha(e.createdAt)}</Dato>
        {e.revisadaPor && <Dato etiqueta="Revisada por">{`${e.revisadaPor} · ${fmtFecha(e.fechaRevision)}`}</Dato>}
        {e.horasVentana != null && <Dato etiqueta="Ventana de ofertas">{e.horasVentana} horas</Dato>}
        <div className="sm:col-span-2 lg:col-span-3">
          <Dato etiqueta="Descripción">
            <span className="whitespace-pre-line">{e.descripcion}</span>
          </Dato>
        </div>
        {e.observacionesRevision && (
          <div className="sm:col-span-2 lg:col-span-3">
            <Dato etiqueta="Observaciones del CCR">
              <span className="whitespace-pre-line">{e.observacionesRevision}</span>
            </Dato>
          </div>
        )}
      </dl>
    </Tarjeta>
  )
}
