package ar.edu.unlp.dssd.rescuesync.emergencia;

/**
 * Espejo del avance del caso en Bonita, usado para filtrar y mostrar.
 * No decide qué paso sigue: esa decisión es del BPM.
 */
public enum EstadoEmergencia {
    /** Registrada por el municipio, pendiente de revisión del CCR. */
    REGISTRADA,
    /** Revisada por el CCR; se están definiendo los lotes de necesidades. */
    EN_REVISION,
    /** Lotes y ventana definidos; pendiente de publicación de la convocatoria. */
    LOTES_DEFINIDOS,
    CONVOCATORIA_ABIERTA,
    CONVOCATORIA_CERRADA,
    EN_VALIDACION,
    EN_ADJUDICACION,
    EN_EJECUCION,
    CERRADA;

    /** Estados a partir de los cuales la emergencia es visible para las ONGs de la red. */
    public boolean esPublica() {
        return ordinal() >= CONVOCATORIA_ABIERTA.ordinal();
    }
}
