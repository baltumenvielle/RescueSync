package ar.edu.unlp.dssd.rescuesync.bpm;

/** Falla al interactuar con el motor de procesos. El mensaje debe ser legible para el usuario. */
public class BpmException extends RuntimeException {

    public BpmException(String mensaje) {
        super(mensaje);
    }

    public BpmException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
