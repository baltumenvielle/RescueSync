import { createBrowserRouter } from 'react-router'
import { InicioSegunRol, RequiereRol, RequiereSesion } from './auth/rutas'
import { Layout } from './components/Layout'
import { EventosAuditoria } from './pages/auditor/EventosAuditoria'
import { DecisionCobertura } from './pages/ccr/DecisionCobertura'
import { DesgloseLotes } from './pages/ccr/DesgloseLotes'
import { RevisionEmergencia } from './pages/ccr/RevisionEmergencia'
import { Bandeja } from './pages/Bandeja'
import { EmergenciaDetalle } from './pages/EmergenciaDetalle'
import { Emergencias } from './pages/Emergencias'
import { Login } from './pages/Login'
import { NuevaEmergencia } from './pages/municipio/NuevaEmergencia'
import { NoEncontrado } from './pages/NoEncontrado'
import { Convocatorias } from './pages/ong/Convocatorias'
import { FormularioOferta } from './pages/ong/FormularioOferta'
import { MisOfertas } from './pages/ong/MisOfertas'
import { VersionesOferta } from './pages/VersionesOferta'

export const router = createBrowserRouter([
  { path: '/login', element: <Login /> },
  {
    path: '/',
    element: (
      <RequiereSesion>
        <Layout />
      </RequiereSesion>
    ),
    children: [
      { index: true, element: <InicioSegunRol /> },
      {
        path: 'tareas',
        element: (
          <RequiereRol roles={['OPERADOR_MUNICIPAL', 'CCR', 'REPRESENTANTE_ONG']}>
            <Bandeja />
          </RequiereRol>
        ),
      },
      {
        path: 'emergencias',
        element: (
          <RequiereRol roles={['OPERADOR_MUNICIPAL', 'CCR', 'AUDITOR']}>
            <Emergencias />
          </RequiereRol>
        ),
      },
      {
        path: 'emergencias/nueva',
        element: (
          <RequiereRol roles={['OPERADOR_MUNICIPAL']}>
            <NuevaEmergencia />
          </RequiereRol>
        ),
      },
      { path: 'emergencias/:id', element: <EmergenciaDetalle /> },
      {
        path: 'emergencias/:id/revision',
        element: (
          <RequiereRol roles={['CCR']}>
            <RevisionEmergencia />
          </RequiereRol>
        ),
      },
      {
        path: 'emergencias/:id/desglose',
        element: (
          <RequiereRol roles={['CCR']}>
            <DesgloseLotes />
          </RequiereRol>
        ),
      },
      {
        path: 'emergencias/:id/decision',
        element: (
          <RequiereRol roles={['CCR']}>
            <DecisionCobertura />
          </RequiereRol>
        ),
      },
      {
        path: 'ong/convocatorias',
        element: (
          <RequiereRol roles={['REPRESENTANTE_ONG']}>
            <Convocatorias />
          </RequiereRol>
        ),
      },
      {
        path: 'ong/convocatorias/:emergenciaId/oferta',
        element: (
          <RequiereRol roles={['REPRESENTANTE_ONG']}>
            <FormularioOferta />
          </RequiereRol>
        ),
      },
      {
        path: 'ong/ofertas',
        element: (
          <RequiereRol roles={['REPRESENTANTE_ONG']}>
            <MisOfertas />
          </RequiereRol>
        ),
      },
      {
        path: 'ofertas/:id/versiones',
        element: (
          <RequiereRol roles={['REPRESENTANTE_ONG', 'CCR', 'AUDITOR']}>
            <VersionesOferta />
          </RequiereRol>
        ),
      },
      {
        path: 'auditoria/eventos',
        element: (
          <RequiereRol roles={['AUDITOR']}>
            <EventosAuditoria />
          </RequiereRol>
        ),
      },
      { path: '*', element: <NoEncontrado /> },
    ],
  },
])
