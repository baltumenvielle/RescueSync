import { useQuery } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router'
import { mensajeDeError } from '../api/client'
import { api } from '../api/endpoints'
import { useAuth } from '../auth/AuthContext'
import { inicioSegunRol } from '../lib/roles'
import { PantallaAcceso } from '../components/PantallaAcceso'
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
  const registroDisponible = useQuery({ queryKey: ['registro-disponible'], queryFn: api.registroDisponible, staleTime: Infinity })

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
    <PantallaAcceso>
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
          {registroDisponible.data && (
            <p className="mt-4 text-center text-sm text-slate-600">
              ¿Necesita otra cuenta?{' '}
              <Link to="/registro" className="font-semibold text-marca-700 hover:underline">
                Crear cuenta de prueba
              </Link>
            </p>
          )}
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
    </PantallaAcceso>
  )
}
