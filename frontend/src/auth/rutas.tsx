import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import type { Rol } from '../api/types'
import { inicioSegunRol } from '../lib/roles'
import { useAuth } from './AuthContext'

export function RequiereSesion({ children }: { children: ReactNode }) {
  const { usuario } = useAuth()
  const location = useLocation()
  if (!usuario) {
    return <Navigate to="/login" replace state={{ desde: location.pathname }} />
  }
  return children
}

export function RequiereRol({ roles, children }: { roles: Rol[]; children: ReactNode }) {
  const { tieneRol } = useAuth()
  if (!tieneRol(...roles)) {
    return <Navigate to="/" replace />
  }
  return children
}

export function InicioSegunRol() {
  const { usuario } = useAuth()
  return <Navigate to={usuario ? inicioSegunRol(usuario.rol) : '/login'} replace />
}
