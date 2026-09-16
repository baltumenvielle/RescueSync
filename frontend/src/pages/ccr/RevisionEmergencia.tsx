import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { mensajeDeError } from '../../api/client'
import { api } from '../../api/endpoints'
import { ResumenEmergencia } from '../../components/ResumenEmergencia'
import { Alerta, AreaTexto, Boton, BotonLink, Campo, Cargando, Encabezado, Tarjeta } from '../../components/ui'

export function RevisionEmergencia() {
  const id = Number(useParams().id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [observaciones, setObservaciones] = useState('')
  const emergencia = useQuery({ queryKey: ['emergencia', id], queryFn: () => api.emergencia(id) })

  const revisar = useMutation({
    mutationFn: () => api.revisar(id, observaciones),
    onSuccess: (e) => {
      queryClient.setQueryData(['emergencia', id], e)
      queryClient.invalidateQueries({ queryKey: ['tareas'] })
      navigate(`/emergencias/${id}/desglose`)
    },
  })

  if (emergencia.isPending) return <Cargando />
  if (!emergencia.data) return <Alerta>{mensajeDeError(emergencia.error)}</Alerta>
  const e = emergencia.data

  return (
    <>
      <Encabezado titulo="Revisión de información" subtitulo={`Emergencia #${e.id} · ${e.municipio.nombre}`} volver={{ to: '/tareas', texto: 'Bandeja de tareas' }} />
      <div className="grid gap-5">
        <ResumenEmergencia emergencia={e} />
        {e.estado !== 'REGISTRADA' ? (
          <Alerta tipo="info" titulo="La emergencia ya fue revisada">
            <BotonLink to={`/emergencias/${id}`} variante="secundario" className="mt-2">
              Ver emergencia
            </BotonLink>
          </Alerta>
        ) : (
          <Tarjeta titulo="Confirmar revisión">
            <div className="grid gap-4">
              {revisar.isError && <Alerta>{mensajeDeError(revisar.error)}</Alerta>}
              <Campo etiqueta="Observaciones (opcional)" ayuda="Verificaciones realizadas, contactos, correcciones a la información del municipio.">
                {(fid, desc) => <AreaTexto id={fid} aria-describedby={desc} rows={4} maxLength={4000} value={observaciones} onChange={(ev) => setObservaciones(ev.target.value)} />}
              </Campo>
              <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
                <BotonLink to="/tareas" variante="secundario">
                  Volver
                </BotonLink>
                <Boton cargando={revisar.isPending} onClick={() => revisar.mutate()}>
                  Confirmar y pasar al desglose
                </Boton>
              </div>
            </div>
          </Tarjeta>
        )}
      </div>
    </>
  )
}
