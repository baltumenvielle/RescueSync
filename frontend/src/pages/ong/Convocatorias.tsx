import { useQuery, useQueryClient } from '@tanstack/react-query'
import { mensajeDeError } from '../../api/client'
import { api } from '../../api/endpoints'
import { EstadoOfertaBadge, GravedadBadge } from '../../components/badges'
import { CuentaRegresiva } from '../../components/CuentaRegresiva'
import { Alerta, BotonLink, Cargando, Encabezado, Vacio } from '../../components/ui'
import { fmtFecha, fmtNumero, TIPOS_DESASTRE } from '../../lib/formato'

export function Convocatorias() {
  const queryClient = useQueryClient()
  const convocatorias = useQuery({ queryKey: ['convocatorias'], queryFn: api.convocatorias, refetchInterval: 30_000 })

  return (
    <>
      <Encabezado titulo="Convocatorias abiertas" subtitulo="Emergencias que están recibiendo ofertas de ayuda. Puede ofrecer cobertura parcial de uno o más lotes." />
      {convocatorias.isPending && <Cargando />}
      {convocatorias.isError && <Alerta>{mensajeDeError(convocatorias.error)}</Alerta>}
      {convocatorias.data?.length === 0 && <Vacio titulo="No hay convocatorias abiertas">Cuando el Centro Coordinador publique una, aparecerá aquí.</Vacio>}
      <ul className="grid gap-4">
        {convocatorias.data?.map((c) => (
          <li key={c.emergencia.id} className="rounded-xl bg-white shadow-sm ring-1 ring-slate-200">
            <div className="flex flex-col gap-3 border-b border-slate-100 p-4 sm:flex-row sm:items-start sm:justify-between sm:p-5">
              <div className="min-w-0">
                <p className="font-semibold text-slate-900">
                  #{c.emergencia.id} · {TIPOS_DESASTRE[c.emergencia.tipoDesastre]} en {c.emergencia.zonaAfectada}
                </p>
                <p className="mt-0.5 text-sm text-slate-500">{c.emergencia.municipio.nombre}</p>
                <div className="mt-2 flex flex-wrap gap-2">
                  <GravedadBadge gravedad={c.emergencia.gravedad} />
                  {c.miOferta && <EstadoOfertaBadge estado={c.miOferta.estado} />}
                </div>
              </div>
              <div className="rounded-lg bg-slate-50 px-3 py-2 text-left sm:text-right">
                <p className="text-xs text-slate-500">Cierra {fmtFecha(c.fechaCierre)}</p>
                <CuentaRegresiva hasta={c.fechaCierre} className="text-lg" alVencer={() => queryClient.invalidateQueries({ queryKey: ['convocatorias'] })} />
              </div>
            </div>
            <div className="grid gap-4 p-4 sm:p-5 md:grid-cols-[1fr_auto] md:items-end">
              <div>
                <p className="line-clamp-3 text-sm text-slate-700">{c.descripcion}</p>
                <p className="mt-3 text-xs font-medium uppercase tracking-wide text-slate-500">Necesidades</p>
                <ul className="mt-1 flex flex-wrap gap-2">
                  {c.lotes.map((l) => (
                    <li key={l.id} className="rounded-md bg-slate-100 px-2 py-1 text-sm text-slate-700">
                      {fmtNumero(l.cantidadRequerida)} {l.unidad} · {l.descripcion}
                    </li>
                  ))}
                </ul>
              </div>
              <BotonLink to={`/ong/convocatorias/${c.emergencia.id}/oferta`} variante={c.miOferta ? 'secundario' : 'primario'} className="w-full md:w-auto">
                {c.miOferta ? `Editar oferta (v${c.miOferta.versionActual})` : 'Cargar oferta'}
              </BotonLink>
            </div>
          </li>
        ))}
      </ul>
    </>
  )
}
