import type { Rol } from '../api/types'

/** Pantalla inicial de cada perfil. */
export function inicioSegunRol(rol: Rol): string {
  switch (rol) {
    case 'OPERADOR_MUNICIPAL':
      return '/emergencias'
    case 'CCR':
      return '/tareas'
    case 'REPRESENTANTE_ONG':
      return '/ong/convocatorias'
    case 'AUDITOR':
      return '/emergencias'
  }
}
