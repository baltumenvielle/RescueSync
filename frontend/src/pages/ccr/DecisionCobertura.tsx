import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { mensajeDeError } from '../../api/client'
import { api } from '../../api/endpoints'
import type { DecisionCcr } from '../../api/types'
import { TablaCobertura } from '../../components/Cobertura'
import { Alerta, Boton, Campo, Cargando, Encabezado, Entrada, Tarjeta } from '../../components/ui'
import { cx } from '../../lib/cx'
import { DECISIONES, TIPOS_DESASTRE } from '../../lib/formato'

const DESCRIPCIONES: Record<DecisionCcr, string> = {
  REABRIR: 'Se publica nuevamente la convocatoria con los mismos lotes y una nueva ventana. Las ofertas existentes se conservan y pueden actualizarse.',
  REFORMULAR: 'Vuelve al desglose para ajustar cantidades o lotes antes de publicar otra vez.',
  CONTINUAR_PARCIAL: 'Se avanza a la validación externa con las ofertas recibidas, aunque no cubran todos los lotes.',
}

export function DecisionCobertura() {
  const id = Number(useParams().id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [decision, setDecision] = useState<DecisionCcr | null>(null)
  const [horas, setHoras] = useState<number>(24)

  const emergencia = useQuery({ queryKey: ['emergencia', id], queryFn: () => api.emergencia(id) })
  const cobertura = useQuery({ queryKey: ['cobertura', id], queryFn: () => api.cobertura(id) })

  const decidir = useMutation({
    mutationFn: () => api.decidirCobertura(id, decision!, decision === 'REABRIR' ? horas : undefined),
    onSuccess: (e) => {
      queryClient.setQueryData(['emergencia', id], e)
      queryClient.invalidateQueries({ queryKey: ['tareas'] })
      navigate(decision === 'REFORMULAR' ? `/emergencias/${id}/desglose` : `/emergencias/${id}`)
    },
  })

  if (emergencia.isPending) return <Cargando />
  if (!emergencia.data) return <Alerta>{mensajeDeError(emergencia.error)}</Alerta>
  const e = emergencia.data
  const horasValidas = Number.isInteger(horas) && horas >= 1 && horas <= 720

  return (
    <>
      <Encabezado titulo="Cobertura insuficiente" subtitulo={`Emergencia #${e.id} · ${TIPOS_DESASTRE[e.tipoDesastre]} en ${e.zonaAfectada}`} volver={{ to: '/tareas', texto: 'Bandeja de tareas' }} />
      {e.estado !== 'CONVOCATORIA_CERRADA' ? (
        <Alerta tipo="info">No hay una decisión pendiente para esta emergencia.</Alerta>
      ) : (
        <div className="grid gap-5 lg:grid-cols-5">
          <Tarjeta titulo="Cobertura al cierre de la ventana" className="lg:col-span-3">
            {cobertura.data ? <TablaCobertura cobertura={cobertura.data} /> : <Cargando />}
          </Tarjeta>
          <Tarjeta titulo="Curso de acción" className="lg:col-span-2">
            <fieldset className="grid gap-3">
              <legend className="sr-only">Decisión</legend>
              {(Object.keys(DECISIONES) as DecisionCcr[]).map((d) => (
                <label key={d} className={cx('flex cursor-pointer gap-3 rounded-lg p-3 ring-1 ring-inset transition', decision === d ? 'bg-marca-50 ring-marca-500' : 'ring-slate-200 hover:bg-slate-50')}>
                  <input type="radio" name="decision" className="mt-1 accent-marca-600" checked={decision === d} onChange={() => setDecision(d)} />
                  <span>
                    <span className="block text-sm font-semibold text-slate-900">{DECISIONES[d]}</span>
                    <span className="mt-0.5 block text-sm text-slate-600">{DESCRIPCIONES[d]}</span>
                  </span>
                </label>
              ))}
            </fieldset>
            {decision === 'REABRIR' && (
              <Campo etiqueta="Duración de la nueva ventana (horas)" error={horasValidas ? undefined : 'Entre 1 y 720 horas'} className="mt-4">
                {(fid) => <Entrada id={fid} type="number" min={1} max={720} inputMode="numeric" invalido={!horasValidas} value={Number.isNaN(horas) ? '' : horas} onChange={(ev) => setHoras(ev.target.valueAsNumber)} />}
              </Campo>
            )}
            {decidir.isError && (
              <div className="mt-4">
                <Alerta>{mensajeDeError(decidir.error)}</Alerta>
              </div>
            )}
            <Boton className="mt-5 w-full" disabled={!decision || (decision === 'REABRIR' && !horasValidas)} cargando={decidir.isPending} onClick={() => decidir.mutate()}>
              Confirmar decisión
            </Boton>
          </Tarjeta>
        </div>
      )}
    </>
  )
}
