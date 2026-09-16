import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router'
import { erroresDeCampo, mensajeDeError } from '../../api/client'
import { api } from '../../api/endpoints'
import type { NuevaEmergencia as Form } from '../../api/types'
import { Alerta, AreaTexto, Boton, Campo, Encabezado, Entrada, Selector, Tarjeta } from '../../components/ui'
import { GRAVEDADES, opciones, TIPOS_DESASTRE } from '../../lib/formato'

const INICIAL: Form = { tipoDesastre: '', gravedad: '', zonaAfectada: '', descripcion: '' }

function validar(f: Form): Record<string, string> {
  const errores: Record<string, string> = {}
  if (!f.tipoDesastre) errores.tipoDesastre = 'Indique el tipo de desastre'
  if (!f.gravedad) errores.gravedad = 'Indique la gravedad'
  if (!f.zonaAfectada.trim()) errores.zonaAfectada = 'Indique la zona afectada'
  else if (f.zonaAfectada.length > 300) errores.zonaAfectada = 'La zona afectada admite hasta 300 caracteres'
  const largo = f.descripcion.trim().length
  if (largo < 20 || largo > 4000) errores.descripcion = 'La descripción debe tener entre 20 y 4000 caracteres'
  return errores
}

export function NuevaEmergencia() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [form, setForm] = useState<Form>(INICIAL)
  const [errores, setErrores] = useState<Record<string, string>>({})
  const [intentado, setIntentado] = useState(false)

  const registrar = useMutation({
    mutationFn: api.registrarEmergencia,
    onSuccess: (e) => {
      queryClient.invalidateQueries({ queryKey: ['emergencias'] })
      navigate(`/emergencias/${e.id}`, { state: { registrada: true } })
    },
    onError: (err) => setErrores(erroresDeCampo(err)),
  })

  const cambiar = <K extends keyof Form>(campo: K, valor: Form[K]) => {
    const nuevo = { ...form, [campo]: valor }
    setForm(nuevo)
    if (intentado) setErrores(validar(nuevo))
  }

  function enviar(e: FormEvent) {
    e.preventDefault()
    setIntentado(true)
    const encontrados = validar(form)
    setErrores(encontrados)
    if (Object.keys(encontrados).length === 0) registrar.mutate(form)
  }

  return (
    <>
      <Encabezado
        titulo="Registrar emergencia"
        subtitulo="Al registrarla se notifica al Centro Coordinador Regional para su revisión."
        volver={{ to: '/emergencias', texto: 'Mis emergencias' }}
      />
      <Tarjeta className="max-w-3xl">
        <form onSubmit={enviar} noValidate className="grid gap-5">
          {registrar.isError && Object.keys(erroresDeCampo(registrar.error)).length === 0 && (
            <Alerta>{mensajeDeError(registrar.error)}</Alerta>
          )}
          <div className="grid gap-5 sm:grid-cols-2">
            <Campo etiqueta="Tipo de desastre" error={errores.tipoDesastre}>
              {(id, desc) => (
                <Selector id={id} aria-describedby={desc} invalido={!!errores.tipoDesastre} value={form.tipoDesastre} onChange={(e) => cambiar('tipoDesastre', e.target.value as Form['tipoDesastre'])}>
                  <option value="">Seleccione…</option>
                  {opciones(TIPOS_DESASTRE).map((o) => (
                    <option key={o.valor} value={o.valor}>
                      {o.etiqueta}
                    </option>
                  ))}
                </Selector>
              )}
            </Campo>
            <fieldset>
              <legend className="mb-1 block text-sm font-medium text-slate-700">Nivel de gravedad</legend>
              <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
                {opciones(GRAVEDADES).map((o) => (
                  <label
                    key={o.valor}
                    className={`flex min-h-10 cursor-pointer items-center justify-center rounded-lg px-2 text-sm font-medium ring-1 ring-inset transition ${
                      form.gravedad === o.valor
                        ? o.valor === 'CRITICA'
                          ? 'bg-red-600 text-white ring-red-600'
                          : 'bg-slate-900 text-white ring-slate-900'
                        : errores.gravedad
                          ? 'bg-white text-slate-700 ring-red-400'
                          : 'bg-white text-slate-700 ring-slate-300 hover:bg-slate-50'
                    }`}
                  >
                    <input type="radio" name="gravedad" className="sr-only" value={o.valor} checked={form.gravedad === o.valor} onChange={() => cambiar('gravedad', o.valor)} />
                    {o.etiqueta}
                  </label>
                ))}
              </div>
              {errores.gravedad && <p className="mt-1 text-sm text-red-600">{errores.gravedad}</p>}
            </fieldset>
          </div>
          <Campo etiqueta="Zona afectada" error={errores.zonaAfectada} ayuda="Barrios, localidades o coordenadas de referencia.">
            {(id, desc) => (
              <Entrada id={id} aria-describedby={desc} invalido={!!errores.zonaAfectada} maxLength={300} value={form.zonaAfectada} onChange={(e) => cambiar('zonaAfectada', e.target.value)} />
            )}
          </Campo>
          <Campo etiqueta="Descripción inicial" error={errores.descripcion} ayuda={`${form.descripcion.trim().length}/4000 caracteres. Incluya personas afectadas, accesos y necesidades urgentes.`}>
            {(id, desc) => (
              <AreaTexto id={id} aria-describedby={desc} invalido={!!errores.descripcion} rows={6} maxLength={4000} value={form.descripcion} onChange={(e) => cambiar('descripcion', e.target.value)} />
            )}
          </Campo>
          <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
            <Boton type="button" variante="secundario" onClick={() => navigate('/emergencias')}>
              Cancelar
            </Boton>
            <Boton type="submit" cargando={registrar.isPending}>
              Registrar emergencia
            </Boton>
          </div>
        </form>
      </Tarjeta>
    </>
  )
}
