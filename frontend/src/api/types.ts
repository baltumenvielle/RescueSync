// Tipos que reflejan los DTO del backend.

export type Rol = 'OPERADOR_MUNICIPAL' | 'CCR' | 'REPRESENTANTE_ONG' | 'AUDITOR'
export type TipoOrganizacion = 'MUNICIPIO' | 'CCR' | 'ONG' | 'AUDITORIA'
export type TipoDesastre =
  | 'INUNDACION'
  | 'INCENDIO'
  | 'TERREMOTO'
  | 'TORNADO'
  | 'TEMPORAL'
  | 'SEQUIA'
  | 'DESLIZAMIENTO'
  | 'OTRO'
export type Gravedad = 'BAJA' | 'MEDIA' | 'ALTA' | 'CRITICA'
export type EstadoEmergencia =
  | 'REGISTRADA'
  | 'EN_REVISION'
  | 'LOTES_DEFINIDOS'
  | 'CONVOCATORIA_ABIERTA'
  | 'CONVOCATORIA_CERRADA'
  | 'EN_VALIDACION'
  | 'EN_ADJUDICACION'
  | 'EN_EJECUCION'
  | 'CERRADA'
export type DecisionCcr = 'REABRIR' | 'REFORMULAR' | 'CONTINUAR_PARCIAL'
export type TipoLote = 'PERSONAL' | 'RECURSO'
export type Prioridad = 'BAJA' | 'MEDIA' | 'ALTA'
export type EstadoOferta = 'BORRADOR' | 'ENVIADA' | 'VALIDADA' | 'ADJUDICADA' | 'NO_ADJUDICADA'
export type AccionTarea = 'REGISTRO' | 'REVISION' | 'DESGLOSE' | 'EVALUAR_COBERTURA' | 'OTRA'

export interface Organizacion {
  id: number
  nombre: string
  tipo: TipoOrganizacion
}

export interface Usuario {
  id: number
  username: string
  nombre: string
  email: string
  rol: Rol
  organizacion: Organizacion
}

export interface LoginResponse {
  token: string
  expiraEn: string
  usuario: Usuario
}

export interface Lote {
  id: number
  tipo: TipoLote
  descripcion: string
  unidad: string
  cantidadRequerida: number
  prioridad: Prioridad
}

export interface EmergenciaResumen {
  id: number
  tipoDesastre: TipoDesastre
  gravedad: Gravedad
  zonaAfectada: string
  estado: EstadoEmergencia
  municipio: Organizacion
  createdAt: string
  fechaCierreConvocatoria: string | null
}

export interface EmergenciaDetalle {
  id: number
  tipoDesastre: TipoDesastre
  gravedad: Gravedad
  zonaAfectada: string
  descripcion: string
  estado: EstadoEmergencia
  municipio: Organizacion
  caseId: string | null
  createdAt: string
  observacionesRevision: string | null
  revisadaPor: string | null
  fechaRevision: string | null
  horasVentana: number | null
  fechaAperturaConvocatoria: string | null
  fechaCierreConvocatoria: string | null
  decisionCcr: DecisionCcr | null
  admiteEdicionDeLotes: boolean
  lotes: Lote[]
}

export interface Tarea {
  taskId: string
  nombreTarea: string
  accion: AccionTarea
  caseId: string
  emergenciaId: number | null
  fechaDisponible: string | null
  emergencia: EmergenciaResumen | null
}

export interface CoberturaLote {
  loteId: number
  tipo: TipoLote
  descripcion: string
  unidad: string
  cantidadRequerida: number
  cantidadOfrecida: number
  porcentaje: number
  cubierto: boolean
  ofertasQueAportan: number
}

export interface Cobertura {
  completa: boolean
  lotesTotales: number
  lotesCubiertos: number
  lotes: CoberturaLote[]
}

export interface ItemOferta {
  loteId: number
  tipo: TipoLote
  descripcionLote: string
  unidad: string
  cantidadRequerida: number
  cantidadOfrecida: number
  rol: 'PRINCIPAL' | 'APOYO' | null
}

export interface VersionOferta {
  numero: number
  autor: string
  comentario: string | null
  createdAt: string
  items: ItemOferta[]
}

export interface Oferta {
  id: number
  emergencia: EmergenciaResumen
  ong: Organizacion
  estado: EstadoOferta
  versionActual: number
  createdAt: string
  editable: boolean
  vigente: VersionOferta
}

export interface Convocatoria {
  emergencia: EmergenciaResumen
  descripcion: string
  fechaApertura: string
  fechaCierre: string
  lotes: Lote[]
  miOferta: { id: number; estado: EstadoOferta; versionActual: number } | null
}

export interface EventoAuditoria {
  id: number
  entidad: string
  entidadId: number | null
  accion: string
  usuarioId: number | null
  payload: string | null
  timestamp: string
}

export interface PaginaEventos {
  contenido: EventoAuditoria[]
  pagina: number
  tamanio: number
  total: number
}

export interface Problema {
  status: number
  title?: string
  detail?: string
  errores?: Record<string, string>
  codigo?: string
}

export interface NuevaEmergencia {
  tipoDesastre: TipoDesastre | ''
  gravedad: Gravedad | ''
  zonaAfectada: string
  descripcion: string
}

export interface LoteForm {
  tipo: TipoLote
  descripcion: string
  unidad: string
  cantidadRequerida: number
  prioridad: Prioridad
}

export interface OfertaForm {
  comentario: string
  items: { loteId: number; cantidad: number }[]
}

export interface RegistroForm {
  nombre: string
  email: string
  username: string
  password: string
  rol: Rol
  organizacionId: number | null
  nuevaOrganizacion: string | null
}
