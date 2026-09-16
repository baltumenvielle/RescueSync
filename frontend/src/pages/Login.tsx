import { useState, type FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router'
import { mensajeDeError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { inicioSegunRol } from '../lib/roles'
import { Alerta, Boton, Campo, Entrada } from '../components/ui'

const USUARIOS_DEMO = [
  ['municipio.laplata', 'Operador municipal'],
  ['ccr', 'Centro Coordinador'],
  ['ong.cruzroja', 'ONG'],
  ['auditor', 'Auditor'],
] as const

export function Login() {
  const { usuario, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  if (usuario) return <Navigate to={inicioSegunRol(usuario.rol)} replace />

  async function enviar(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setEnviando(true)
    try {
      const u = await login(username, password)
      const desde = (location.state as { desde?: string } | null)?.desde
      navigate(desde && desde !== '/' ? desde : inicioSegunRol(u.rol), { replace: true })
    } catch (err) {
      setError(mensajeDeError(err))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="flex min-h-dvh flex-col bg-slate-900 lg:flex-row">
      <div className="flex flex-col justify-center px-6 pt-10 pb-6 sm:px-10 lg:w-1/2 lg:px-16">
        <div className="flex items-center gap-3">
          <svg viewBox="0 0 32 32" className="size-10" aria-hidden>
            <rect width="32" height="32" rx="7" fill="#f97316" />
            <path d="M13 7h6v6h6v6h-6v6h-6v-6H7v-6h6z" fill="#fff" />
          </svg>
          <span className="text-2xl font-bold text-white">RescueSync</span>
        </div>
        <h1 className="mt-6 max-w-md text-2xl font-semibold leading-tight text-white sm:text-3xl lg:text-4xl">
          Coordinación regional de la respuesta ante desastres.
        </h1>
        <p className="mt-3 max-w-md text-slate-400">
          Municipios, Centro Coordinador y ONGs trabajando sobre la misma emergencia, desde el registro hasta el cierre.
        </p>
      </div>
      <div className="flex flex-1 items-start justify-center px-4 pb-10 sm:px-6 lg:items-center lg:bg-slate-100 lg:py-10">
        <form onSubmit={enviar} className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl sm:p-8" noValidate>
          <h2 className="text-lg font-semibold text-slate-900">Iniciar sesión</h2>
          <div className="mt-5 flex flex-col gap-4">
            {error && <Alerta>{error}</Alerta>}
            <Campo etiqueta="Usuario">
              {(id) => (
                <Entrada id={id} autoComplete="username" autoCapitalize="none" value={username} onChange={(e) => setUsername(e.target.value)} required />
              )}
            </Campo>
            <Campo etiqueta="Contraseña">
              {(id) => (
                <Entrada id={id} type="password" autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} required />
              )}
            </Campo>
            <Boton type="submit" cargando={enviando} disabled={!username || !password} className="w-full">
              Ingresar
            </Boton>
          </div>
          {import.meta.env.DEV && (
            <div className="mt-6 border-t border-slate-100 pt-4">
              <p className="text-xs font-medium text-slate-500">Usuarios de prueba (contraseña: rescuesync)</p>
              <div className="mt-2 flex flex-wrap gap-1.5">
                {USUARIOS_DEMO.map(([u, rol]) => (
                  <button
                    key={u}
                    type="button"
                    className="rounded-md bg-slate-100 px-2 py-1 text-xs text-slate-700 hover:bg-slate-200"
                    onClick={() => {
                      setUsername(u)
                      setPassword('rescuesync')
                    }}
                  >
                    {rol}
                  </button>
                ))}
              </div>
            </div>
          )}
        </form>
      </div>
    </div>
  )
}
