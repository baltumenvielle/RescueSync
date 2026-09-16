import { http } from './client'
import type {
  Cobertura,
  Convocatoria,
  DecisionCcr,
  EmergenciaDetalle,
  EmergenciaResumen,
  EstadoEmergencia,
  Lote,
  LoteForm,
  LoginResponse,
  NuevaEmergencia,
  Oferta,
  OfertaForm,
  PaginaEventos,
  Tarea,
  Usuario,
  VersionOferta,
} from './types'

const datos = <T>(p: Promise<{ data: T }>) => p.then((r) => r.data)

export const api = {
  login: (username: string, password: string) =>
    datos(http.post<LoginResponse>('/auth/login', { username, password })),
  logout: () => http.post('/auth/logout'),
  me: () => datos(http.get<Usuario>('/auth/me')),

  tareas: () => datos(http.get<Tarea[]>('/tareas')),

  emergencias: (estado?: EstadoEmergencia) =>
    datos(http.get<EmergenciaResumen[]>('/emergencias', { params: { estado } })),
  emergencia: (id: number) => datos(http.get<EmergenciaDetalle>(`/emergencias/${id}`)),
  registrarEmergencia: (body: NuevaEmergencia) => datos(http.post<EmergenciaDetalle>('/emergencias', body)),

  revisar: (id: number, observaciones: string) =>
    datos(http.put<EmergenciaDetalle>(`/emergencias/${id}/revision`, { observaciones })),
  crearLote: (id: number, body: LoteForm) => datos(http.post<Lote>(`/emergencias/${id}/lotes`, body)),
  actualizarLote: (id: number, loteId: number, body: LoteForm) =>
    datos(http.put<Lote>(`/emergencias/${id}/lotes/${loteId}`, body)),
  eliminarLote: (id: number, loteId: number) => http.delete(`/emergencias/${id}/lotes/${loteId}`),
  confirmarConvocatoria: (id: number, horasVentana: number) =>
    datos(http.post<EmergenciaDetalle>(`/emergencias/${id}/convocatoria`, { horasVentana })),
  cobertura: (id: number) => datos(http.get<Cobertura>(`/emergencias/${id}/cobertura`)),
  decidirCobertura: (id: number, decision: DecisionCcr, horasVentana?: number) =>
    datos(http.post<EmergenciaDetalle>(`/emergencias/${id}/decision-cobertura`, { decision, horasVentana })),
  ofertasDeEmergencia: (id: number) => datos(http.get<Oferta[]>(`/emergencias/${id}/ofertas`)),

  convocatorias: () => datos(http.get<Convocatoria[]>('/ong/convocatorias')),
  misOfertas: () => datos(http.get<Oferta[]>('/ong/ofertas')),
  oferta: (id: number) => datos(http.get<Oferta>(`/ofertas/${id}`)),
  versiones: (id: number) => datos(http.get<VersionOferta[]>(`/ofertas/${id}/versiones`)),
  crearOferta: (emergenciaId: number, body: OfertaForm) =>
    datos(http.post<Oferta>(`/emergencias/${emergenciaId}/ofertas`, body)),
  editarOferta: (id: number, body: OfertaForm) => datos(http.put<Oferta>(`/ofertas/${id}`, body)),
  enviarOferta: (id: number) => datos(http.post<Oferta>(`/ofertas/${id}/enviar`)),

  eventos: (params: { entidad?: string; entidadId?: number; page?: number; size?: number }) =>
    datos(http.get<PaginaEventos>('/auditoria/eventos', { params })),

  // Solo disponible con FakeBpmAdapter (sin Bonita)
  devBpmDisponible: () =>
    http.get('/dev/bpm').then(
      () => true,
      () => false,
    ),
  devPublicar: (id: number) => http.post(`/dev/bpm/emergencias/${id}/publicar`),
  devFinVentana: (id: number) => datos(http.post<Cobertura>(`/dev/bpm/emergencias/${id}/fin-ventana`)),
}
