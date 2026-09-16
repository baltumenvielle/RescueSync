import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router'
import { erroresDeCampo, mensajeDeError } from '../../api/client'
import { api } from '../../api/endpoints'
import type { Lote, LoteForm } from '../../api/types'
import { TablaLotes } from '../../components/TablaLotes'
import { Alerta, Boton, BotonLink, Campo, Cargando, Encabezado, Entrada, Selector, Tarjeta, Vacio } from '../../components/ui'
import { fmtNumero, opciones, PRIORIDADES, TIPOS_DESASTRE, TIPOS_LOTE } from '../../lib/formato'

const LOTE_VACIO: LoteForm = { tipo: 'RECURSO', descripcion: '', unidad: '', cantidadRequerida: 1, prioridad: 'MEDIA' }

function FormularioLote({ emergenciaId, lote, alTerminar }: { emergenciaId: number; lote: Lote | null; alTerminar: () => void }) {
  const queryClient = useQueryClient()
  const [form, setForm] = useState<LoteForm>(lote ? { ...lote } : LOTE_VACIO)
  const guardar = useMutation({
    mutationFn: () => (lote ? api.actualizarLote(emergenciaId, lote.id, form) : api.crearLote(emergenciaId, form)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['emergencia', emergenciaId] })
      setForm(LOTE_VACIO)
      alTerminar()
    },
  })
  const errores = erroresDeCampo(guardar.error)

  function enviar(e: FormEvent) {
    e.preventDefault()
    guardar.mutate()
  }

  return (
    <form onSubmit={enviar} className="grid gap-4" noValidate>
      {guardar.isError && Object.keys(errores).length === 0 && <Alerta>{mensajeDeError(guardar.error)}</Alerta>}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-6">
        <Campo etiqueta="Descripción" error={errores.descripcion} className="sm:col-span-2 lg:col-span-3">
          {(id) => <Entrada id={id} placeholder="Ej: Paramédicos, raciones de alimento" maxLength={300} invalido={!!errores.descripcion} value={form.descripcion} onChange={(e) => setForm({ ...form, descripcion: e.target.value })} />}
        </Campo>
        <Campo etiqueta="Tipo" className="lg:col-span-1">
          {(id) => (
            <Selector id={id} value={form.tipo} onChange={(e) => setForm({ ...form, tipo: e.target.value as LoteForm['tipo'] })}>
              {opciones(TIPOS_LOTE).map((o) => (
                <option key={o.valor} value={o.valor}>
                  {o.etiqueta}
                </option>
              ))}
            </Selector>
          )}
        </Campo>
        <Campo etiqueta="Prioridad" className="lg:col-span-2">
          {(id) => (
            <Selector id={id} value={form.prioridad} onChange={(e) => setForm({ ...form, prioridad: e.target.value as LoteForm['prioridad'] })}>
              {opciones(PRIORIDADES).map((o) => (
                <option key={o.valor} value={o.valor}>
                  {o.etiqueta}
                </option>
              ))}
            </Selector>
          )}
        </Campo>
        <Campo etiqueta="Cantidad requerida" error={errores.cantidadRequerida} className="lg:col-span-2">
          {(id) => <Entrada id={id} type="number" inputMode="numeric" min={1} invalido={!!errores.cantidadRequerida} value={form.cantidadRequerida || ''} onChange={(e) => setForm({ ...form, cantidadRequerida: Number(e.target.value) })} />}
        </Campo>
        <Campo etiqueta="Unidad" error={errores.unidad} className="lg:col-span-2">
          {(id) => <Entrada id={id} placeholder="personas, raciones, litros…" maxLength={50} invalido={!!errores.unidad} value={form.unidad} onChange={(e) => setForm({ ...form, unidad: e.target.value })} />}
        </Campo>
        <div className="flex items-end gap-2 sm:col-span-2 lg:col-span-2">
          {lote && (
            <Boton type="button" variante="secundario" onClick={alTerminar} className="flex-1">
              Cancelar
            </Boton>
          )}
          <Boton type="submit" cargando={guardar.isPending} className="flex-1">
            {lote ? 'Guardar cambios' : 'Agregar lote'}
          </Boton>
        </div>
      </div>
    </form>
  )
}

export function DesgloseLotes() {
  const id = Number(useParams().id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const emergencia = useQuery({ queryKey: ['emergencia', id], queryFn: () => api.emergencia(id) })
  const [editando, setEditando] = useState<Lote | null>(null)
  const [horas, setHoras] = useState<number>(48)
  const [confirmando, setConfirmando] = useState(false)
  const [ahora] = useState(() => Date.now())

  const eliminar = useMutation({
    mutationFn: (loteId: number) => api.eliminarLote(id, loteId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['emergencia', id] }),
  })
  const confirmar = useMutation({
    mutationFn: () => api.confirmarConvocatoria(id, horas),
    onSuccess: (e) => {
      queryClient.setQueryData(['emergencia', id], e)
      queryClient.invalidateQueries({ queryKey: ['tareas'] })
      navigate(`/emergencias/${id}`)
    },
  })

  if (emergencia.isPending) return <Cargando />
  if (!emergencia.data) return <Alerta>{mensajeDeError(emergencia.error)}</Alerta>
  const e = emergencia.data
  const horasValidas = Number.isInteger(horas) && horas >= 1 && horas <= 720
  const cierreEstimado = new Date(ahora + horas * 3_600_000)

  return (
    <>
      <Encabezado
        titulo="Desglose en lotes de necesidades"
        subtitulo={`Emergencia #${e.id} · ${TIPOS_DESASTRE[e.tipoDesastre]} en ${e.zonaAfectada}`}
        volver={{ to: `/emergencias/${id}`, texto: 'Emergencia' }}
      />
      {!e.admiteEdicionDeLotes ? (
        <Alerta tipo="info" titulo="Los lotes ya no pueden modificarse">
          La emergencia está en otra etapa del proceso.
        </Alerta>
      ) : (
        <div className="grid gap-5">
          <Tarjeta titulo={editando ? `Editar lote: ${editando.descripcion}` : 'Nuevo lote'}>
            <FormularioLote key={editando?.id ?? 'nuevo'} emergenciaId={id} lote={editando} alTerminar={() => setEditando(null)} />
          </Tarjeta>

          <Tarjeta titulo={`Lotes definidos (${e.lotes.length})`}>
            {eliminar.isError && (
              <div className="mb-3">
                <Alerta>{mensajeDeError(eliminar.error)}</Alerta>
              </div>
            )}
            {e.lotes.length === 0 ? (
              <Vacio titulo="Sin lotes">Agregue al menos un lote para poder publicar la convocatoria.</Vacio>
            ) : (
              <TablaLotes
                lotes={e.lotes}
                acciones={(l) => (
                  <span className="inline-flex gap-1">
                    <Boton variante="fantasma" className="min-h-8 px-2 py-1" onClick={() => setEditando(l)}>
                      Editar
                    </Boton>
                    <Boton
                      variante="fantasma"
                      className="min-h-8 px-2 py-1 text-red-700"
                      cargando={eliminar.isPending && eliminar.variables === l.id}
                      onClick={() => {
                        if (window.confirm(`¿Eliminar el lote "${l.descripcion}"?`)) eliminar.mutate(l.id)
                      }}
                    >
                      Eliminar
                    </Boton>
                  </span>
                )}
              />
            )}
          </Tarjeta>

          <Tarjeta titulo="Ventana de recepción de ofertas">
            <div className="grid gap-4">
              {confirmar.isError && <Alerta>{mensajeDeError(confirmar.error)}</Alerta>}
              <div className="grid gap-4 sm:grid-cols-2 sm:items-end">
                <Campo etiqueta="Duración (horas)" error={horasValidas ? undefined : 'Entre 1 y 720 horas'} ayuda={horasValidas ? `Si se publica ahora, cerraría aprox. el ${cierreEstimado.toLocaleString('es-AR', { dateStyle: 'short', timeStyle: 'short' })}.` : undefined}>
                  {(fid, desc) => <Entrada id={fid} aria-describedby={desc} type="number" inputMode="numeric" min={1} max={720} invalido={!horasValidas} value={Number.isNaN(horas) ? '' : horas} onChange={(ev) => setHoras(ev.target.valueAsNumber)} />}
                </Campo>
                <div className="flex flex-wrap gap-2">
                  {[12, 24, 48, 72].map((h) => (
                    <button key={h} type="button" onClick={() => setHoras(h)} className={`min-h-10 rounded-lg px-3 text-sm font-medium ring-1 ring-inset ${horas === h ? 'bg-slate-900 text-white ring-slate-900' : 'bg-white text-slate-700 ring-slate-300 hover:bg-slate-50'}`}>
                      {h} h
                    </button>
                  ))}
                </div>
              </div>

              {confirmando ? (
                <Alerta tipo="aviso" titulo="¿Confirmar lotes y publicar la convocatoria?">
                  <p>
                    Se publicarán {e.lotes.length} lotes (
                    {e.lotes.map((l) => `${fmtNumero(l.cantidadRequerida)} ${l.unidad} de ${l.descripcion.toLowerCase()}`).join('; ')}) a toda la red, con una ventana de {horas} horas. Luego no podrán editarse.
                  </p>
                  <div className="mt-3 flex flex-col-reverse gap-2 sm:flex-row">
                    <Boton variante="secundario" onClick={() => setConfirmando(false)}>
                      Seguir editando
                    </Boton>
                    <Boton cargando={confirmar.isPending} onClick={() => confirmar.mutate()}>
                      Confirmar y publicar
                    </Boton>
                  </div>
                </Alerta>
              ) : (
                <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
                  <BotonLink to="/tareas" variante="secundario">
                    Continuar más tarde
                  </BotonLink>
                  <Boton disabled={e.lotes.length === 0 || !horasValidas} onClick={() => setConfirmando(true)}>
                    Publicar convocatoria…
                  </Boton>
                </div>
              )}
            </div>
          </Tarjeta>
        </div>
      )}
    </>
  )
}
