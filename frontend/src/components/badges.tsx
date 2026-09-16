import type { EstadoEmergencia, EstadoOferta, Gravedad } from '../api/types'
import { ESTADOS_EMERGENCIA, ESTADOS_OFERTA, GRAVEDADES } from '../lib/formato'
import { Etiqueta } from './ui'

const TONO_ESTADO: Record<EstadoEmergencia, Parameters<typeof Etiqueta>[0]['tono']> = {
  REGISTRADA: 'azul',
  EN_REVISION: 'violeta',
  LOTES_DEFINIDOS: 'violeta',
  CONVOCATORIA_ABIERTA: 'naranja',
  CONVOCATORIA_CERRADA: 'ambar',
  EN_VALIDACION: 'azul',
  EN_ADJUDICACION: 'azul',
  EN_EJECUCION: 'verde',
  CERRADA: 'gris',
}

const TONO_GRAVEDAD: Record<Gravedad, Parameters<typeof Etiqueta>[0]['tono']> = {
  BAJA: 'gris',
  MEDIA: 'ambar',
  ALTA: 'naranja',
  CRITICA: 'rojo',
}

export const EstadoEmergenciaBadge = ({ estado }: { estado: EstadoEmergencia }) => (
  <Etiqueta tono={TONO_ESTADO[estado]}>{ESTADOS_EMERGENCIA[estado]}</Etiqueta>
)

export const GravedadBadge = ({ gravedad }: { gravedad: Gravedad }) => (
  <Etiqueta tono={TONO_GRAVEDAD[gravedad]}>Gravedad {GRAVEDADES[gravedad].toLowerCase()}</Etiqueta>
)

export const EstadoOfertaBadge = ({ estado }: { estado: EstadoOferta }) => (
  <Etiqueta tono={estado === 'BORRADOR' ? 'gris' : estado === 'ENVIADA' ? 'verde' : 'azul'}>{ESTADOS_OFERTA[estado]}</Etiqueta>
)
