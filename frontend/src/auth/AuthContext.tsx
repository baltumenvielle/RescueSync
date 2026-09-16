import { useQueryClient } from '@tanstack/react-query'
import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { CLAVE_SESION, onSesionExpirada } from '../api/client'
import { api } from '../api/endpoints'
import type { Rol, Usuario } from '../api/types'

interface Sesion {
  token: string
  expiraEn: string
  usuario: Usuario
}

interface AuthValor {
  usuario: Usuario | null
  login: (username: string, password: string) => Promise<Usuario>
  logout: () => void
  tieneRol: (...roles: Rol[]) => boolean
}

const AuthContext = createContext<AuthValor | null>(null)

function leerSesion(): Sesion | null {
  try {
    const crudo = localStorage.getItem(CLAVE_SESION)
    if (!crudo) return null
    const sesion = JSON.parse(crudo) as Sesion
    return new Date(sesion.expiraEn) > new Date() ? sesion : null
  } catch {
    return null
  }
}

function guardarSesion(sesion: Sesion | null) {
  try {
    if (sesion) localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion))
    else localStorage.removeItem(CLAVE_SESION)
  } catch {
    // almacenamiento no disponible: la sesión dura lo que la pestaña
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [sesion, setSesion] = useState<Sesion | null>(leerSesion)
  const queryClient = useQueryClient()

  const cerrarLocal = useCallback(() => {
    guardarSesion(null)
    setSesion(null)
    queryClient.clear()
  }, [queryClient])

  useEffect(() => onSesionExpirada(cerrarLocal), [cerrarLocal])

  const login = useCallback(async (username: string, password: string) => {
    const r = await api.login(username, password)
    const nueva = { token: r.token, expiraEn: r.expiraEn, usuario: r.usuario }
    guardarSesion(nueva)
    setSesion(nueva)
    return r.usuario
  }, [])

  const logout = useCallback(() => {
    api.logout().catch(() => undefined)
    cerrarLocal()
  }, [cerrarLocal])

  const valor = useMemo<AuthValor>(
    () => ({
      usuario: sesion?.usuario ?? null,
      login,
      logout,
      tieneRol: (...roles) => !!sesion && roles.includes(sesion.usuario.rol),
    }),
    [sesion, login, logout],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth fuera de AuthProvider')
  return ctx
}
