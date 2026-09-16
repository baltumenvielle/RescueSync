import { BotonLink, Vacio } from '../components/ui'

export function NoEncontrado() {
  return (
    <Vacio titulo="Página no encontrada">
      <p className="mb-4">La dirección no existe o no tiene acceso.</p>
      <BotonLink to="/" variante="secundario">
        Ir al inicio
      </BotonLink>
    </Vacio>
  )
}
