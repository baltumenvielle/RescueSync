import axios, { AxiosError } from 'axios'
import type { Problema } from './types'

const CLAVE_SESION = 'rescuesync.sesion'

export const http = axios.create({ baseURL: '/api' })

let alExpirar: (() => void) | null = null

export function onSesionExpirada(callback: () => void) {
  alExpirar = callback
}

export function leerToken(): string | null {
  try {
    const crudo = localStorage.getItem(CLAVE_SESION)
    return crudo ? (JSON.parse(crudo).token as string) : null
  } catch {
    return null
  }
}

export { CLAVE_SESION }

http.interceptors.request.use((config) => {
  const token = leerToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (r) => r,
  (error: AxiosError<Problema>) => {
    const url = error.config?.url ?? ''
    if (error.response?.status === 401 && !url.includes('/auth/login')) {
      alExpirar?.()
    }
    return Promise.reject(error)
  },
)

/** Mensaje legible a partir de un error de la API. */
export function mensajeDeError(error: unknown): string {
  if (axios.isAxiosError<Problema>(error)) {
    const problema = error.response?.data
    if (problema?.detail) return problema.detail
    if (!error.response) return 'No se pudo conectar con el servidor'
    return `Error ${error.response.status}`
  }
  return error instanceof Error ? error.message : 'Error inesperado'
}

export function erroresDeCampo(error: unknown): Record<string, string> {
  if (axios.isAxiosError<Problema>(error)) {
    return error.response?.data?.errores ?? {}
  }
  return {}
}
