package ar.edu.unlp.dssd.rescuesync.common;

/** El usuario está autenticado pero el recurso no pertenece a su organización (403). */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
