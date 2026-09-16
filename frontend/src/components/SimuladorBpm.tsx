import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { mensajeDeError } from '../api/client'
import { api } from '../api/endpoints'
import type { EmergenciaDetalle } from '../api/types'
import { Alerta, Boton, Tarjeta } from './ui'

/**
 * Solo visible cuando el backend corre con FakeBpmAdapter: dispara lo que en Bonita hacen
 * automáticamente la publicación de la convocatoria y el timer de la ventana.
 */
export function SimuladorBpm({ emergencia }: { emergencia: EmergenciaDetalle }) {
  const queryClient = useQueryClient()
  const disponible = useQuery({ queryKey: ['dev-bpm'], queryFn: api.devBpmDisponible, staleTime: Infinity })
  const refrescar = () => queryClient.invalidateQueries()

  const publicar = useMutation({ mutationFn: () => api.devPublicar(emergencia.id), onSuccess: refrescar })
  const finVentana = useMutation({ mutationFn: () => api.devFinVentana(emergencia.id), onSuccess: refrescar })

  const puedePublicar = emergencia.estado === 'LOTES_DEFINIDOS'
  const puedeCerrar = emergencia.estado === 'CONVOCATORIA_ABIERTA'
  if (!disponible.data || (!puedePublicar && !puedeCerrar)) return null

  const error = publicar.error ?? finVentana.error
  return (
    <Tarjeta titulo="Simulador del motor de procesos" className="border-2 border-dashed border-violet-300 shadow-none ring-0">
      <p className="mb-3 text-sm text-slate-600">
        El backend corre sin Bonita (FakeBpmAdapter). Estos botones reemplazan a la tarea automática de publicación y al vencimiento del timer.
      </p>
      {error && (
        <div className="mb-3">
          <Alerta>{mensajeDeError(error)}</Alerta>
        </div>
      )}
      {finVentana.data && (
        <div className="mb-3">
          <Alerta tipo={finVentana.data.completa ? 'exito' : 'aviso'}>
            {finVentana.data.completa ? 'Cobertura completa: el proceso sigue a la validación externa.' : 'Cobertura insuficiente: se generó la tarea de decisión para el CCR.'}
          </Alerta>
        </div>
      )}
      <div className="flex flex-wrap gap-2">
        {puedePublicar && (
          <Boton variante="secundario" cargando={publicar.isPending} onClick={() => publicar.mutate()}>
            Publicar convocatoria
          </Boton>
        )}
        {puedeCerrar && (
          <Boton variante="secundario" cargando={finVentana.isPending} onClick={() => finVentana.mutate()}>
            Simular fin de ventana
          </Boton>
        )}
      </div>
    </Tarjeta>
  )
}
