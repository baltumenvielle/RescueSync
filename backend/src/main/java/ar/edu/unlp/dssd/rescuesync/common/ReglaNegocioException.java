package ar.edu.unlp.dssd.rescuesync.common;

/** Operación válida sintácticamente pero no permitida por el estado del dominio (409). */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
