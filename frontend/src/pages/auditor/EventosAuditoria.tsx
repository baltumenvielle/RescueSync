import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { mensajeDeError } from '../../api/client'
import { api } from '../../api/endpoints'
import { Alerta, Boton, Cargando, Encabezado, Entrada, Selector, Tarjeta, Vacio } from '../../components/ui'
import { fmtFecha } from '../../lib/formato'

const ENTIDADES = ['EMERGENCIA', 'LOTE', 'OFERTA']
const TAMANIO = 25

export function EventosAuditoria() {
  const [entidad, setEntidad] = useState('')
  const [entidadId, setEntidadId] = useState('')
  const [pagina, setPagina] = useState(0)

  const eventos = useQuery({
    queryKey: ['eventos', entidad, entidadId, pagina],
    queryFn: () => api.eventos({ entidad: entidad || undefined, entidadId: entidad && entidadId ? Number(entidadId) : undefined, page: pagina, size: TAMANIO }),
    placeholderData: keepPreviousData,
  })
  const total = eventos.data?.total ?? 0
  const paginas = Math.max(1, Math.ceil(total / TAMANIO))

  return (
    <>
      <Encabezado titulo="Eventos de auditoría" subtitulo="Registro inmutable de las operaciones sobre emergencias, lotes y ofertas." />
      <Tarjeta>
        <div className="mb-4 grid gap-3 sm:grid-cols-[12rem_10rem]">
          <Selector aria-label="Entidad" value={entidad} onChange={(e) => { setEntidad(e.target.value); setPagina(0) }}>
            <option value="">Todas las entidades</option>
            {ENTIDADES.map((e) => (
              <option key={e} value={e}>
                {e}
              </option>
            ))}
          </Selector>
          <Entrada aria-label="Id de la entidad" placeholder="Id" type="number" inputMode="numeric" disabled={!entidad} value={entidadId} onChange={(e) => { setEntidadId(e.target.value); setPagina(0) }} />
        </div>
        {eventos.isPending && <Cargando />}
        {eventos.isError && <Alerta>{mensajeDeError(eventos.error)}</Alerta>}
        {eventos.data?.contenido.length === 0 && <Vacio titulo="Sin eventos" />}
        {eventos.data && eventos.data.contenido.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[40rem] text-left text-sm">
              <thead className="text-xs uppercase tracking-wide text-slate-500">
                <tr>
                  <th className="py-2 pr-4 font-medium">Fecha</th>
                  <th className="py-2 pr-4 font-medium">Entidad</th>
                  <th className="py-2 pr-4 font-medium">Acción</th>
                  <th className="py-2 pr-4 font-medium">Usuario</th>
                  <th className="py-2 font-medium">Detalle</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 align-top">
                {eventos.data.contenido.map((ev) => (
                  <tr key={ev.id}>
                    <td className="py-2 pr-4 whitespace-nowrap text-slate-600">{fmtFecha(ev.timestamp)}</td>
                    <td className="py-2 pr-4 whitespace-nowrap">
                      {ev.entidad} <span className="text-slate-500">#{ev.entidadId}</span>
                    </td>
                    <td className="py-2 pr-4 font-medium whitespace-nowrap">{ev.accion}</td>
                    <td className="py-2 pr-4 text-slate-600">{ev.usuarioId ?? 'BPM'}</td>
                    <td className="py-2">
                      {ev.payload && <code className="block max-w-md truncate text-xs text-slate-600" title={ev.payload}>{ev.payload}</code>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <div className="mt-4 flex items-center justify-between gap-2 text-sm text-slate-600">
          <span>
            {total} eventos · página {pagina + 1} de {paginas}
          </span>
          <div className="flex gap-2">
            <Boton variante="secundario" disabled={pagina === 0} onClick={() => setPagina((p) => p - 1)}>
              Anterior
            </Boton>
            <Boton variante="secundario" disabled={pagina + 1 >= paginas} onClick={() => setPagina((p) => p + 1)}>
              Siguiente
            </Boton>
          </div>
        </div>
      </Tarjeta>
    </>
  )
}
