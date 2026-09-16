import type {
  AccionTarea,
  DecisionCcr,
  EstadoEmergencia,
  EstadoOferta,
  Gravedad,
  Prioridad,
  Rol,
  TipoDesastre,
  TipoLote,
} from '../api/types'

export const ROLES: Record<Rol, string> = {
  OPERADOR_MUNICIPAL: 'Operador municipal',
  CCR: 'Centro Coordinador Regional',
  REPRESENTANTE_ONG: 'Representante de ONG',
  AUDITOR: 'Auditor / Directivo',
}

export const TIPOS_DESASTRE: Record<TipoDesastre, string> = {
  INUNDACION: 'Inundación',
  INCENDIO: 'Incendio',
  TERREMOTO: 'Terremoto',
  TORNADO: 'Tornado',
  TEMPORAL: 'Temporal',
  SEQUIA: 'Sequía',
  DESLIZAMIENTO: 'Deslizamiento',
  OTRO: 'Otro',
}

export const GRAVEDADES: Record<Gravedad, string> = {
  BAJA: 'Baja',
  MEDIA: 'Media',
  ALTA: 'Alta',
  CRITICA: 'Crítica',
}

export const ESTADOS_EMERGENCIA: Record<EstadoEmergencia, string> = {
  REGISTRADA: 'Registrada',
  EN_REVISION: 'Desglose de lotes',
  LOTES_DEFINIDOS: 'Pendiente de publicación',
  CONVOCATORIA_ABIERTA: 'Convocatoria abierta',
  CONVOCATORIA_CERRADA: 'Convocatoria cerrada',
  EN_VALIDACION: 'En validación',
  EN_ADJUDICACION: 'En adjudicación',
  EN_EJECUCION: 'En ejecución',
  CERRADA: 'Cerrada',
}

export const ESTADOS_OFERTA: Record<EstadoOferta, string> = {
  BORRADOR: 'Borrador',
  ENVIADA: 'Enviada',
  VALIDADA: 'Validada',
  ADJUDICADA: 'Adjudicada',
  NO_ADJUDICADA: 'No adjudicada',
}

export const DECISIONES: Record<DecisionCcr, string> = {
  REABRIR: 'Reabrir la convocatoria',
  REFORMULAR: 'Reformular los lotes',
  CONTINUAR_PARCIAL: 'Continuar con cobertura parcial',
}

export const TIPOS_LOTE: Record<TipoLote, string> = { PERSONAL: 'Personal', RECURSO: 'Recurso' }
export const PRIORIDADES: Record<Prioridad, string> = { BAJA: 'Baja', MEDIA: 'Media', ALTA: 'Alta' }

export const ACCIONES_TAREA: Record<AccionTarea, string> = {
  REGISTRO: 'Registrar emergencia',
  REVISION: 'Revisar información',
  DESGLOSE: 'Definir lotes y publicar',
  EVALUAR_COBERTURA: 'Decidir ante cobertura insuficiente',
  OTRA: 'Ver emergencia',
}

const fechaHora = new Intl.DateTimeFormat('es-AR', { dateStyle: 'short', timeStyle: 'short' })
const numero = new Intl.NumberFormat('es-AR')

export const fmtFecha = (iso: string | null | undefined) => (iso ? fechaHora.format(new Date(iso)) : '—')
export const fmtNumero = (n: number) => numero.format(n)

export function opciones<T extends string>(etiquetas: Record<T, string>) {
  return (Object.entries(etiquetas) as [T, string][]).map(([valor, etiqueta]) => ({ valor, etiqueta }))
}
