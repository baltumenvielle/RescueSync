import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { mensajeDeError } from '../../api/client'
import { api } from '../../api/endpoints'
import { EstadoOfertaBadge } from '../../components/badges'
import { Alerta, BotonLink, Cargando, Encabezado, Vacio } from '../../components/ui'
import { fmtFecha, fmtNumero, TIPOS_DESASTRE } from '../../lib/formato'

export function MisOfertas() {
  const ofertas = useQuery({ queryKey: ['mis-ofertas'], queryFn: api.misOfertas })

  return (
    <>
      <Encabezado titulo="Mis ofertas" subtitulo="Ofertas de ayuda registradas por su organización." />
      {ofertas.isPending && <Cargando />}
      {ofertas.isError && <Alerta>{mensajeDeError(ofertas.error)}</Alerta>}
      {ofertas.data?.length === 0 && (
        <Vacio titulo="Todavía no registró ofertas">
          <Link to="/ong/convocatorias" className="font-medium text-marca-700 underline">
            Ver convocatorias abiertas
          </Link>
        </Vacio>
      )}
      <ul className="grid gap-3">
        {ofertas.data?.map((o) => (
          <li key={o.id} className="rounded-xl bg-white p-4 shadow-sm ring-1 ring-slate-200 sm:p-5">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
              <div className="min-w-0">
                <p className="font-semibold text-slate-900">
                  #{o.emergencia.id} · {TIPOS_DESASTRE[o.emergencia.tipoDesastre]} en {o.emergencia.zonaAfectada}
                </p>
                <div className="mt-1 flex flex-wrap items-center gap-2 text-sm text-slate-500">
                  <EstadoOfertaBadge estado={o.estado} />
                  <span>Versión {o.versionActual}</span>
                  <span aria-hidden>·</span>
                  <span>Última modificación {fmtFecha(o.vigente.createdAt)}</span>
                </div>
                <ul className="mt-3 flex flex-wrap gap-2">
                  {o.vigente.items.map((i) => (
                    <li key={i.loteId} className="rounded-md bg-slate-100 px-2 py-1 text-sm text-slate-700">
                      {fmtNumero(i.cantidadOfrecida)}/{fmtNumero(i.cantidadRequerida)} {i.unidad} · {i.descripcionLote}
                    </li>
                  ))}
                </ul>
              </div>
              <div className="flex shrink-0 flex-wrap gap-2">
                <BotonLink to={`/ofertas/${o.id}/versiones`} variante="secundario">
                  Historial
                </BotonLink>
                {o.editable && <BotonLink to={`/ong/convocatorias/${o.emergencia.id}/oferta`}>Editar</BotonLink>}
              </div>
            </div>
          </li>
        ))}
      </ul>
    </>
  )
}
