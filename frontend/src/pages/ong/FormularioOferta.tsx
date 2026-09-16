import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useCallback, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { mensajeDeError } from '../../api/client'
import { api } from '../../api/endpoints'
import type { Convocatoria, Oferta, OfertaForm } from '../../api/types'
import { EstadoOfertaBadge } from '../../components/badges'
import { CuentaRegresiva } from '../../components/CuentaRegresiva'
import { Alerta, AreaTexto, Boton, BotonLink, Campo, Cargando, Encabezado, Entrada, Tarjeta } from '../../components/ui'
import { cx } from '../../lib/cx'
import { fmtNumero, TIPOS_DESASTRE, TIPOS_LOTE } from '../../lib/formato'

type Cantidades = Record<number, string>

export function FormularioOferta() {
  const emergenciaId = Number(useParams().emergenciaId)
  const convocatorias = useQuery({ queryKey: ['convocatorias'], queryFn: api.convocatorias })
  const convocatoria = convocatorias.data?.find((c) => c.emergencia.id === emergenciaId)
  const ofertaId = convocatoria?.miOferta?.id
  const oferta = useQuery({ queryKey: ['oferta', ofertaId], queryFn: () => api.oferta(ofertaId!), enabled: !!ofertaId })
  // Vive fuera del editor para no perder el aviso cuando el editor se reinicia tras crear la oferta.
  const [guardada, setGuardada] = useState<Oferta | null>(null)

  if (convocatorias.isPending || (ofertaId && oferta.isPending)) return <Cargando />
  if (convocatorias.isError) return <Alerta>{mensajeDeError(convocatorias.error)}</Alerta>
  if (ofertaId && oferta.isError) return <Alerta>{mensajeDeError(oferta.error)}</Alerta>
  if (!convocatoria) {
    return (
      <>
        <Encabezado titulo="Oferta de ayuda" volver={{ to: '/ong/convocatorias', texto: 'Convocatorias' }} />
        <Alerta tipo="info" titulo="La convocatoria no está abierta">
          La ventana de recepción de ofertas cerró o la emergencia no existe. Puede consultar sus ofertas en <Link to="/ong/ofertas" className="font-medium underline">Mis ofertas</Link>.
        </Alerta>
      </>
    )
  }
  return (
    <EditorOferta
      key={ofertaId ?? 'nueva'}
      convocatoria={convocatoria}
      actual={oferta.data ?? null}
      guardada={guardada}
      setGuardada={setGuardada}
    />
  )
}

interface EditorProps {
  convocatoria: Convocatoria
  actual: Oferta | null
  guardada: Oferta | null
  setGuardada: (o: Oferta | null) => void
}

function cantidadesIniciales(oferta: Oferta | null): Cantidades {
  const iniciales: Cantidades = {}
  oferta?.vigente.items.forEach((i) => (iniciales[i.loteId] = String(i.cantidadOfrecida)))
  return iniciales
}

function EditorOferta({ convocatoria, actual, guardada, setGuardada }: EditorProps) {
  const emergenciaId = convocatoria.emergencia.id
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [cantidades, setCantidades] = useState<Cantidades>(() => cantidadesIniciales(actual))
  const [comentario, setComentario] = useState('')
  const [vencida, setVencida] = useState(false)
  const [errorLocal, setErrorLocal] = useState<string | null>(null)

  const alVencer = useCallback(() => setVencida(true), [])

  const alGuardar = (o: Oferta) => {
    queryClient.setQueryData(['oferta', o.id], o)
    queryClient.invalidateQueries({ queryKey: ['convocatorias'] })
    queryClient.invalidateQueries({ queryKey: ['mis-ofertas'] })
    queryClient.invalidateQueries({ queryKey: ['versiones', o.id] })
    setGuardada(o)
  }

  const guardar = useMutation({
    mutationFn: async (enviar: boolean) => {
      const body: OfertaForm = {
        comentario,
        items: Object.entries(cantidades)
          .filter(([, v]) => v !== '' && Number(v) > 0)
          .map(([loteId, v]) => ({ loteId: Number(loteId), cantidad: Number(v) })),
      }
      let resultado = actual ? await api.editarOferta(actual.id, body) : await api.crearOferta(emergenciaId, body)
      if (enviar && resultado.estado === 'BORRADOR') {
        resultado = await api.enviarOferta(resultado.id)
      }
      return resultado
    },
    onSuccess: (o) => {
      setComentario('')
      alGuardar(o)
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
  })

  const enviarSola = useMutation({
    mutationFn: () => api.enviarOferta(actual!.id),
    onSuccess: alGuardar,
  })

  const seleccionados = Object.values(cantidades).filter((v) => v !== '' && Number(v) > 0).length
  const cantidadesInvalidas = Object.values(cantidades).some((v) => v !== '' && (!Number.isInteger(Number(v)) || Number(v) < 0))
  const bloqueado = vencida || guardar.isPending

  function accion(enviar: boolean) {
    setErrorLocal(null)
    setGuardada(null)
    if (seleccionados === 0) return setErrorLocal('Indique una cantidad para al menos un lote.')
    if (cantidadesInvalidas) return setErrorLocal('Las cantidades deben ser números enteros positivos.')
    guardar.mutate(enviar)
  }

  return (
    <>
      <Encabezado
        titulo={actual ? `Editar oferta · versión ${actual.versionActual}` : 'Nueva oferta de ayuda'}
        subtitulo={`Emergencia #${emergenciaId} · ${TIPOS_DESASTRE[convocatoria.emergencia.tipoDesastre]} en ${convocatoria.emergencia.zonaAfectada}`}
        volver={{ to: '/ong/convocatorias', texto: 'Convocatorias' }}
        acciones={actual && <BotonLink to={`/ofertas/${actual.id}/versiones`} variante="secundario">Historial de versiones</BotonLink>}
      />

      <div className="grid gap-5 lg:grid-cols-3">
        <div className="grid gap-5 lg:col-span-2">
          {guardada && (
            <Alerta tipo="exito" titulo={guardada.estado === 'ENVIADA' ? 'Oferta enviada' : 'Borrador guardado'}>
              Se registró la versión {guardada.versionActual}.{' '}
              {guardada.estado === 'BORRADOR' && 'Recuerde enviarla antes del cierre para que se considere en la cobertura.'}
            </Alerta>
          )}
          {vencida && <Alerta titulo="La ventana de ofertas cerró">Ya no es posible registrar cambios.</Alerta>}
          {(errorLocal || guardar.isError || enviarSola.isError) && <Alerta>{errorLocal ?? mensajeDeError(guardar.error ?? enviarSola.error)}</Alerta>}

          <Tarjeta titulo="Recursos y personal ofrecidos">
            <p className="mb-4 text-sm text-slate-600">Complete solo los lotes que puede cubrir, total o parcialmente. Deje vacíos los demás.</p>
            <ul className="grid gap-3">
              {convocatoria.lotes.map((l) => {
                const valor = cantidades[l.id] ?? ''
                const n = Number(valor)
                const activo = valor !== '' && n > 0
                return (
                  <li key={l.id} className={cx('grid gap-3 rounded-lg p-3 ring-1 ring-inset sm:grid-cols-[1fr_12rem] sm:items-center', activo ? 'bg-marca-50/60 ring-marca-500/40' : 'ring-slate-200')}>
                    <div>
                      <p className="font-medium text-slate-900">{l.descripcion}</p>
                      <p className="text-sm text-slate-500">
                        Requerido: {fmtNumero(l.cantidadRequerida)} {l.unidad} · {TIPOS_LOTE[l.tipo]}
                      </p>
                      {activo && n < l.cantidadRequerida && <p className="mt-0.5 text-xs text-marca-700">Oferta parcial ({Math.floor((n * 100) / l.cantidadRequerida)}%)</p>}
                    </div>
                    <label className="flex items-center gap-2">
                      <span className="sr-only">Cantidad ofrecida de {l.descripcion}</span>
                      <Entrada type="number" inputMode="numeric" min={0} step={1} placeholder="0" disabled={bloqueado} value={valor} onChange={(e) => setCantidades({ ...cantidades, [l.id]: e.target.value })} />
                      <span className="w-20 shrink-0 truncate text-sm text-slate-500">{l.unidad}</span>
                    </label>
                  </li>
                )
              })}
            </ul>
            <Campo etiqueta="Comentario de esta versión (opcional)" className="mt-5" ayuda="Ej: disponibilidad horaria, logística, motivo del cambio.">
              {(id, desc) => <AreaTexto id={id} aria-describedby={desc} rows={3} maxLength={2000} disabled={bloqueado} value={comentario} onChange={(e) => setComentario(e.target.value)} />}
            </Campo>
            <div className="mt-5 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
              {(!actual || actual.estado === 'BORRADOR') && (
                <Boton variante="secundario" disabled={bloqueado} cargando={guardar.isPending && guardar.variables === false} onClick={() => accion(false)}>
                  {actual ? 'Guardar nueva versión' : 'Guardar borrador'}
                </Boton>
              )}
              <Boton disabled={bloqueado} cargando={guardar.isPending && guardar.variables === true} onClick={() => accion(true)}>
                {actual?.estado === 'ENVIADA' ? 'Actualizar oferta enviada' : 'Guardar y enviar'}
              </Boton>
            </div>
          </Tarjeta>
        </div>

        <aside className="order-first grid content-start gap-5 lg:order-none">
          <Tarjeta titulo="Ventana de ofertas">
            <p className="text-sm text-slate-500">Tiempo restante</p>
            <CuentaRegresiva hasta={convocatoria.fechaCierre} alVencer={alVencer} className="text-3xl" />
            <p className="mt-2 text-xs text-slate-500">Cierra el {new Date(convocatoria.fechaCierre).toLocaleString('es-AR', { dateStyle: 'full', timeStyle: 'short' })}</p>
          </Tarjeta>
          {actual && (
            <Tarjeta titulo="Estado de su oferta">
              <div className="flex items-center justify-between">
                <EstadoOfertaBadge estado={actual.estado} />
                <span className="text-sm text-slate-600">Versión {actual.versionActual}</span>
              </div>
              {actual.estado === 'BORRADOR' && (
                <>
                  <p className="mt-3 text-sm text-slate-600">Los borradores no cuentan para la cobertura hasta ser enviados.</p>
                  <Boton className="mt-3 w-full" variante="secundario" disabled={vencida} cargando={enviarSola.isPending} onClick={() => enviarSola.mutate()}>
                    Enviar versión {actual.versionActual} sin cambios
                  </Boton>
                </>
              )}
              {actual.estado === 'ENVIADA' && <p className="mt-3 text-sm text-slate-600">Puede seguir actualizándola hasta el cierre; cada cambio genera una nueva versión.</p>}
            </Tarjeta>
          )}
          <Boton variante="fantasma" onClick={() => navigate('/ong/ofertas')}>
            Ver todas mis ofertas
          </Boton>
        </aside>
      </div>
    </>
  )
}
