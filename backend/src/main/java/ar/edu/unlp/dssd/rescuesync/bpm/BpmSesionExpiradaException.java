package ar.edu.unlp.dssd.rescuesync.bpm;

/** La sesión del usuario contra el motor venció y no pudo renovarse sin su contraseña. */
public class BpmSesionExpiradaException extends BpmException {

    public BpmSesionExpiradaException(String username) {
        super("La sesión de " + username + " en el motor de procesos expiró");
    }
}
