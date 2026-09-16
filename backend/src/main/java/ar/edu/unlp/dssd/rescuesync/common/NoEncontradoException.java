package ar.edu.unlp.dssd.rescuesync.common;

/** Recurso inexistente o fuera del alcance del usuario (se responde 404 en ambos casos). */
public class NoEncontradoException extends RuntimeException {

    public NoEncontradoException(String entidad, Object id) {
        super(entidad + " " + id + " no encontrada");
    }
}
