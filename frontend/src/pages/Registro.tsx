import { useMutation, useQuery } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router'
import { erroresDeCampo, mensajeDeError } from '../api/client'
import { api } from '../api/endpoints'
import type { Rol, TipoOrganizacion } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { PantallaAcceso } from '../components/PantallaAcceso'
import { Alerta, Boton, Campo, Cargando, Entrada, Selector } from '../components/ui'
import { cx } from '../lib/cx'
import { ROLES } from '../lib/formato'
import { inicioSegunRol } from '../lib/roles'

const PERFILES: { rol: Rol; tipo: TipoOrganizacion; descripcion: string }[] = [
  { rol: 'OPERADOR_MUNICIPAL', tipo: 'MUNICIPIO', descripcion: 'Registra emergencias de su municipio' },
  { rol: 'CCR', tipo: 'CCR', descripcion: 'Revisa, desglosa en lotes y publica' },
  { rol: 'REPRESENTANTE_ONG', tipo: 'ONG', descripcion: 'Carga y versiona ofertas de ayuda' },
  { rol: 'AUDITOR', tipo: 'AUDITORIA', descripcion: 'Solo lectura y trazabilidad' },
]

const ETIQUETA_ORGANIZACION: Record<TipoOrganizacion, string> = {
  MUNICIPIO: 'Municipio',
  CCR: 'Centro coordinador',
  ONG: 'ONG',
  AUDITORIA: 'Organismo de auditoría',
}

interface Form {
  nombre: string
  email: string
  username: string
  password: string
  confirmacion: string
  rol: Rol | null
  modoOrganizacion: 'existente' | 'nueva'
  organizacionId: string
  nuevaOrganizacion: string
}

const INICIAL: Form = {
  nombre: '',
  email: '',
  username: '',
  password: '',
  confirmacion: '',
  rol: null,
  modoOrganizacion: 'existente',
  organizacionId: '',
  nuevaOrganizacion: '',
}

function validar(f: Form): Record<string, string> {
  const e: Record<string, string> = {}
  if (!f.rol) e.rol = 'Elija un perfil'
  if (f.modoOrganizacion === 'existente' && !f.organizacionId) e.organizacion = 'Elija una organización'
  if (f.modoOrganizacion === 'nueva' && !f.nuevaOrganizacion.trim()) e.organizacion = 'Indique el nombre de la organización'
  if (!f.nombre.trim()) e.nombre = 'Ingrese su nombre'
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(f.email.trim())) e.email = 'Ingrese un email válido'
  if (!/^[a-z0-9._-]{3,60}$/.test(f.username)) e.username = 'Entre 3 y 60 caracteres: minúsculas, números, punto, guion o guion bajo'
  if (f.password.length < 8 || f.password.length > 72) e.password = 'La contraseña debe tener entre 8 y 72 caracteres'
  if (f.confirmacion !== f.password) e.confirmacion = 'Las contraseñas no coinciden'
  return e
}

export function Registro() {
  const { usuario, iniciarSesionCon } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState<Form>(INICIAL)
  const [errores, setErrores] = useState<Record<string, string>>({})
  const [intentado, setIntentado] = useState(false)

  const organizaciones = useQuery({ queryKey: ['registro-organizaciones'], queryFn: api.organizacionesRegistro, retry: false })

  const registrar = useMutation({
    mutationFn: () =>
      api.registrar({
        nombre: form.nombre,
        email: form.email,
        username: form.username,
        password: form.password,
        rol: form.rol!,
        organizacionId: form.modoOrganizacion === 'existente' ? Number(form.organizacionId) : null,
        nuevaOrganizacion: form.modoOrganizacion === 'nueva' ? form.nuevaOrganizacion : null,
      }),
    onSuccess: (r) => navigate(inicioSegunRol(iniciarSesionCon(r).rol), { replace: true }),
    onError: (err) => {
      const delServidor = erroresDeCampo(err)
      const { organizacionId, nuevaOrganizacion, organizacionIndicada, ...resto } = delServidor
      const organizacion = organizacionId ?? nuevaOrganizacion ?? organizacionIndicada
      setErrores(organizacion ? { ...resto, organizacion } : resto)
    },
  })

  if (usuario) return <Navigate to={inicioSegunRol(usuario.rol)} replace />

  const cambiar = (cambios: Partial<Form>) => {
    const nuevo = { ...form, ...cambios }
    setForm(nuevo)
    if (intentado) setErrores(validar(nuevo))
  }

  const tipo = PERFILES.find((p) => p.rol === form.rol)?.tipo
  const disponibles = organizaciones.data?.filter((o) => o.tipo === tipo) ?? []

  function elegirRol(rol: Rol) {
    const nuevoTipo = PERFILES.find((p) => p.rol === rol)!.tipo
    const hayExistentes = organizaciones.data?.some((o) => o.tipo === nuevoTipo) ?? false
    cambiar({ rol, organizacionId: '', modoOrganizacion: hayExistentes ? 'existente' : 'nueva' })
  }

  function enviar(e: FormEvent) {
    e.preventDefault()
    setIntentado(true)
    const encontrados = validar(form)
    setErrores(encontrados)
    if (Object.keys(encontrados).length === 0) registrar.mutate()
  }

  const errorGeneral = registrar.isError && Object.keys(erroresDeCampo(registrar.error)).length === 0

  return (
    <PantallaAcceso>
      <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl sm:p-8">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <h2 className="text-lg font-semibold text-slate-900">Crear cuenta de prueba</h2>
          <span className="rounded-md bg-violet-50 px-2 py-0.5 text-xs font-medium text-violet-800 ring-1 ring-inset ring-violet-600/20">Modo depuración</span>
        </div>
        <p className="mt-1 text-sm text-slate-600">Para probar la aplicación con distintos perfiles. La cuenta queda activa de inmediato.</p>

        {organizaciones.isPending && <Cargando />}
        {organizaciones.isError && (
          <div className="mt-5">
            <Alerta tipo="info" titulo="El registro está deshabilitado">
              El servidor no admite cuentas de prueba en este entorno.{' '}
              <Link to="/login" className="font-medium underline">
                Volver al inicio de sesión
              </Link>
            </Alerta>
          </div>
        )}

        {organizaciones.data && (
          <form onSubmit={enviar} noValidate className="mt-5 grid gap-5">
            {errorGeneral && <Alerta>{mensajeDeError(registrar.error)}</Alerta>}

            <fieldset>
              <legend className="mb-1 block text-sm font-medium text-slate-700">Perfil</legend>
              <div className="grid gap-2 sm:grid-cols-2">
                {PERFILES.map((p) => (
                  <label
                    key={p.rol}
                    className={cx(
                      'flex cursor-pointer gap-3 rounded-lg p-3 ring-1 ring-inset transition',
                      form.rol === p.rol ? 'bg-marca-50 ring-marca-500' : errores.rol ? 'ring-red-400' : 'ring-slate-200 hover:bg-slate-50',
                    )}
                  >
                    <input type="radio" name="rol" className="mt-1 accent-marca-600" checked={form.rol === p.rol} onChange={() => elegirRol(p.rol)} />
                    <span>
                      <span className="block text-sm font-semibold text-slate-900">{ROLES[p.rol]}</span>
                      <span className="block text-xs text-slate-600">{p.descripcion}</span>
                    </span>
                  </label>
                ))}
              </div>
              {errores.rol && <p className="mt-1 text-sm text-red-600">{errores.rol}</p>}
            </fieldset>

            {tipo && (
              <div>
                <div className="mb-1 flex flex-wrap items-center justify-between gap-2">
                  <span className="text-sm font-medium text-slate-700">{ETIQUETA_ORGANIZACION[tipo]}</span>
                  <div className="inline-flex rounded-lg bg-slate-100 p-0.5 text-xs font-medium" role="group" aria-label="Origen de la organización">
                    {(['existente', 'nueva'] as const).map((modo) => (
                      <button
                        key={modo}
                        type="button"
                        aria-pressed={form.modoOrganizacion === modo}
                        disabled={modo === 'existente' && disponibles.length === 0}
                        onClick={() => cambiar({ modoOrganizacion: modo })}
                        className={cx(
                          'rounded-md px-2.5 py-1 transition disabled:cursor-not-allowed disabled:opacity-40',
                          form.modoOrganizacion === modo ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-600 hover:text-slate-900',
                        )}
                      >
                        {modo === 'existente' ? 'Existente' : 'Nueva'}
                      </button>
                    ))}
                  </div>
                </div>
                {form.modoOrganizacion === 'existente' ? (
                  <Selector aria-label={ETIQUETA_ORGANIZACION[tipo]} invalido={!!errores.organizacion} value={form.organizacionId} onChange={(e) => cambiar({ organizacionId: e.target.value })}>
                    <option value="">Seleccione…</option>
                    {disponibles.map((o) => (
                      <option key={o.id} value={o.id}>
                        {o.nombre}
                      </option>
                    ))}
                  </Selector>
                ) : (
                  <Entrada aria-label={`Nombre de la nueva organización (${ETIQUETA_ORGANIZACION[tipo]})`} placeholder="Nombre de la organización" maxLength={200} invalido={!!errores.organizacion} value={form.nuevaOrganizacion} onChange={(e) => cambiar({ nuevaOrganizacion: e.target.value })} />
                )}
                {errores.organizacion && <p className="mt-1 text-sm text-red-600">{errores.organizacion}</p>}
              </div>
            )}

            <div className="grid gap-4 sm:grid-cols-2">
              <Campo etiqueta="Nombre y apellido" error={errores.nombre}>
                {(id, desc) => <Entrada id={id} aria-describedby={desc} autoComplete="name" maxLength={200} invalido={!!errores.nombre} value={form.nombre} onChange={(e) => cambiar({ nombre: e.target.value })} />}
              </Campo>
              <Campo etiqueta="Email" error={errores.email}>
                {(id, desc) => <Entrada id={id} aria-describedby={desc} type="email" autoComplete="email" maxLength={200} invalido={!!errores.email} value={form.email} onChange={(e) => cambiar({ email: e.target.value })} />}
              </Campo>
            </div>

            <Campo etiqueta="Usuario" error={errores.username} ayuda="Minúsculas, números, punto, guion o guion bajo. Ej: ong.prueba">
              {(id, desc) => (
                <Entrada id={id} aria-describedby={desc} autoComplete="username" autoCapitalize="none" spellCheck={false} maxLength={60} invalido={!!errores.username} value={form.username} onChange={(e) => cambiar({ username: e.target.value.toLowerCase().replace(/\s/g, '') })} />
              )}
            </Campo>

            <div className="grid gap-4 sm:grid-cols-2">
              <Campo etiqueta="Contraseña" error={errores.password} ayuda={errores.password ? undefined : 'Mínimo 8 caracteres'}>
                {(id, desc) => <Entrada id={id} aria-describedby={desc} type="password" autoComplete="new-password" maxLength={72} invalido={!!errores.password} value={form.password} onChange={(e) => cambiar({ password: e.target.value })} />}
              </Campo>
              <Campo etiqueta="Repetir contraseña" error={errores.confirmacion}>
                {(id, desc) => <Entrada id={id} aria-describedby={desc} type="password" autoComplete="new-password" maxLength={72} invalido={!!errores.confirmacion} value={form.confirmacion} onChange={(e) => cambiar({ confirmacion: e.target.value })} />}
              </Campo>
            </div>

            {form.rol && form.rol !== 'AUDITOR' && (
              <p className="rounded-lg bg-slate-50 px-3 py-2 text-xs text-slate-600 ring-1 ring-inset ring-slate-200">
                Con Bonita conectado, este usuario también debe existir en la organización de Bonita con la misma contraseña.
              </p>
            )}

            <Boton type="submit" cargando={registrar.isPending} className="w-full">
              Crear cuenta e ingresar
            </Boton>
            <p className="text-center text-sm text-slate-600">
              ¿Ya tiene cuenta?{' '}
              <Link to="/login" className="font-semibold text-marca-700 hover:underline">
                Iniciar sesión
              </Link>
            </p>
          </form>
        )}
      </div>
    </PantallaAcceso>
  )
}
