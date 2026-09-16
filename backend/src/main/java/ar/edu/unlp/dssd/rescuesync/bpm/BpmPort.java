package ar.edu.unlp.dssd.rescuesync.bpm;

import java.util.List;

/**
 * Frontera entre el dominio de RescueSync y el motor de procesos.
 *
 * <p>Servicios y controllers dependen únicamente de esta interfaz. La implementación real
 * ({@code integration.bonita.BonitaBpmAdapter}, perfil {@code bonita}) habla con la API REST
 * de Bonita; {@link ar.edu.unlp.dssd.rescuesync.bpm.fake.FakeBpmAdapter} la reemplaza en
 * desarrollo y tests. Ningún tipo ni JSON de Bonita cruza esta frontera.
 *
 * <p>Errores: las implementaciones lanzan {@link BpmException} ante fallas de comunicación
 * o respuestas inesperadas, y {@link BpmSesionExpiradaException} cuando la sesión del usuario
 * contra el motor venció y no pudo renovarse (la SPA debe pedir un nuevo login).
 */
public interface BpmPort {

    /**
     * Abre la sesión del usuario contra el motor. Se invoca en cada login exitoso de la app
     * con la contraseña en claro, que la implementación no debe persistir.
     */
    void iniciarSesion(UsuarioBpm usuario, String password);

    /** Descarta la sesión del usuario contra el motor (logout de la app). */
    void cerrarSesion(UsuarioBpm usuario);

    /**
     * Instancia un caso del proceso RescueSync inicializando la variable
     * {@value VariablesProceso#EMERGENCIA_ID}.
     *
     * @return el id del caso creado
     */
    String iniciarCaso(UsuarioBpm usuario, Long emergenciaId);

    /** Tareas humanas listas para ejecutar que el usuario puede tomar según el actor mapping. */
    List<TareaPendiente> tareasDe(UsuarioBpm usuario);

    /**
     * Escribe una variable de proceso del caso. Solo tipos simples: {@link String},
     * {@link Integer}, {@link Long}, {@link Double}, {@link Boolean} y {@link java.util.Date}.
     */
    void setVariable(UsuarioBpm usuario, String caseId, String nombre, Object valor);

    /** Asigna la tarea al usuario y la ejecuta (sin contrato: body vacío). */
    void completarTarea(UsuarioBpm usuario, String taskId);
}
