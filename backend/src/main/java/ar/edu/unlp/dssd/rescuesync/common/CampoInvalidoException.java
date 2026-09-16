package ar.edu.unlp.dssd.rescuesync.common;

/** Error de validación asociado a un campo del formulario que solo puede detectarse en el servidor (400). */
public class CampoInvalidoException extends RuntimeException {

    private final String campo;

    public CampoInvalidoException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
