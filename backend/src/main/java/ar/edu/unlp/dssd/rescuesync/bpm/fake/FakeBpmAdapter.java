package ar.edu.unlp.dssd.rescuesync.bpm.fake;

import ar.edu.unlp.dssd.rescuesync.bpm.AccionTarea;
import ar.edu.unlp.dssd.rescuesync.bpm.BpmException;
import ar.edu.unlp.dssd.rescuesync.bpm.BpmPort;
import ar.edu.unlp.dssd.rescuesync.bpm.NombresTareas;
import ar.edu.unlp.dssd.rescuesync.bpm.TareaPendiente;
import ar.edu.unlp.dssd.rescuesync.bpm.UsuarioBpm;
import ar.edu.unlp.dssd.rescuesync.bpm.VariablesProceso;
import ar.edu.unlp.dssd.rescuesync.config.RescueSyncProperties;
import ar.edu.unlp.dssd.rescuesync.usuario.Rol;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Implementación en memoria de {@link BpmPort} para desarrollo y tests (todo perfil distinto de
 * {@code bonita}).
 *
 * <p>Emula de forma simplificada el recorrido de tareas humanas del modelo, registra cada
 * llamada para poder verificarla en los tests y permite simular los eventos que en Bonita
 * disparan el timer y los conectores (ver {@link FakeBpmController}). El estado se pierde al
 * reiniciar la aplicación.
 */
@Component
@Profile("!bonita")
public class FakeBpmAdapter implements BpmPort {

    private static final Logger log = LoggerFactory.getLogger(FakeBpmAdapter.class);

    private final NombresTareas nombres;
    private final boolean empiezaEnRegistro;
    private final Clock clock;

    private final AtomicLong secuenciaCasos = new AtomicLong(1000);
    private final AtomicLong secuenciaTareas = new AtomicLong(5000);
    private final Map<String, CasoFake> casos = new ConcurrentHashMap<>();
    private final Map<String, TareaFake> tareas = new ConcurrentHashMap<>();
    private final List<LlamadaBpm> llamadas = new CopyOnWriteArrayList<>();

    public FakeBpmAdapter(NombresTareas nombres, RescueSyncProperties properties, Clock clock) {
        this.nombres = nombres;
        this.empiezaEnRegistro = properties.bpm().completarRegistroAlInstanciar();
        this.clock = clock;
    }

    @Override
    public void iniciarSesion(UsuarioBpm usuario, String password) {
        registrar("iniciarSesion", usuario, Map.of());
    }

    @Override
    public void cerrarSesion(UsuarioBpm usuario) {
        registrar("cerrarSesion", usuario, Map.of());
    }

    @Override
    public String iniciarCaso(UsuarioBpm usuario, Long emergenciaId) {
        String caseId = String.valueOf(secuenciaCasos.incrementAndGet());
        CasoFake caso = new CasoFake(caseId);
        caso.variables.put(VariablesProceso.EMERGENCIA_ID, emergenciaId);
        casos.put(caseId, caso);
        registrar("iniciarCaso", usuario, Map.of("caseId", caseId, "emergenciaId", emergenciaId));
        crearTarea(caseId, empiezaEnRegistro ? AccionTarea.REGISTRO : AccionTarea.REVISION);
        return caseId;
    }

    @Override
    public List<TareaPendiente> tareasDe(UsuarioBpm usuario) {
        return tareas.values().stream()
                .filter(t -> t.actor == usuario.rol())
                .map(t -> new TareaPendiente(t.taskId, t.nombre, t.caseId,
                        (Long) casos.get(t.caseId).variables.get(VariablesProceso.EMERGENCIA_ID), t.disponibleDesde))
                .toList();
    }

    @Override
    public void setVariable(UsuarioBpm usuario, String caseId, String nombre, Object valor) {
        CasoFake caso = caso(caseId);
        caso.variables.put(nombre, valor);
        registrar("setVariable", usuario, Map.of("caseId", caseId, "nombre", nombre, "valor", valor));
    }

    @Override
    public void completarTarea(UsuarioBpm usuario, String taskId) {
        TareaFake tarea = tareas.get(taskId);
        if (tarea == null) {
            throw new BpmException("La tarea " + taskId + " no existe o ya fue completada");
        }
        if (tarea.actor != usuario.rol()) {
            throw new BpmException("El usuario " + usuario.username() + " no puede ejecutar la tarea " + taskId);
        }
        tareas.remove(taskId);
        registrar("completarTarea", usuario, Map.of("taskId", taskId, "caseId", tarea.caseId, "tarea", tarea.nombre));
        avanzar(tarea);
    }

    // --- Simulación de lo que en Bonita resuelven el timer, los conectores y los gateways ---

    /**
     * Emula el vencimiento del timer de la ventana: el conector de cobertura escribe
     * {@code coberturaCompleta} y, si es falsa, el gateway devuelve la tarea al CCR.
     */
    public void simularFinVentana(String caseId, boolean coberturaCompleta) {
        CasoFake caso = caso(caseId);
        caso.variables.put(VariablesProceso.COBERTURA_COMPLETA, coberturaCompleta);
        registrar("simularFinVentana", null, Map.of("caseId", caseId, "coberturaCompleta", coberturaCompleta));
        if (!coberturaCompleta) {
            crearTarea(caseId, AccionTarea.EVALUAR_COBERTURA);
        }
    }

    public List<LlamadaBpm> llamadas() {
        return Collections.unmodifiableList(new ArrayList<>(llamadas));
    }

    public List<LlamadaBpm> llamadas(String operacion) {
        return llamadas.stream().filter(l -> l.operacion().equals(operacion)).toList();
    }

    public Map<String, Object> variables(String caseId) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(caso(caseId).variables));
    }

    public List<TareaPendiente> tareasDelCaso(String caseId) {
        return tareas.values().stream()
                .filter(t -> t.caseId.equals(caseId))
                .map(t -> new TareaPendiente(t.taskId, t.nombre, t.caseId, null, t.disponibleDesde))
                .toList();
    }

    public void reiniciar() {
        casos.clear();
        tareas.clear();
        llamadas.clear();
    }

    private void avanzar(TareaFake completada) {
        switch (nombres.accionDe(completada.nombre)) {
            case REGISTRO -> crearTarea(completada.caseId, AccionTarea.REVISION);
            case REVISION -> crearTarea(completada.caseId, AccionTarea.DESGLOSE);
            case DESGLOSE -> {
                // Sigue la tarea automática "Publicación de convocatoria" y el timer de la ventana.
            }
            case EVALUAR_COBERTURA -> {
                Object decision = caso(completada.caseId).variables.get(VariablesProceso.DECISION_CCR);
                if ("REFORMULAR".equals(decision)) {
                    crearTarea(completada.caseId, AccionTarea.DESGLOSE);
                }
                // REABRIR vuelve a la publicación automática; CONTINUAR_PARCIAL sigue a la validación externa.
            }
            case OTRA -> {
            }
        }
    }

    private void crearTarea(String caseId, AccionTarea accion) {
        String taskId = String.valueOf(secuenciaTareas.incrementAndGet());
        Rol actor = accion == AccionTarea.REGISTRO ? Rol.OPERADOR_MUNICIPAL : Rol.CCR;
        tareas.put(taskId, new TareaFake(taskId, nombres.nombreDe(accion), caseId, actor, clock.instant()));
    }

    private CasoFake caso(String caseId) {
        CasoFake caso = casos.get(caseId);
        if (caso == null) {
            throw new BpmException("El caso " + caseId + " no existe en el motor (FakeBpmAdapter se reinició?)");
        }
        return caso;
    }

    private void registrar(String operacion, UsuarioBpm usuario, Map<String, Object> argumentos) {
        LlamadaBpm llamada = new LlamadaBpm(operacion, usuario != null ? usuario.username() : null, argumentos,
                clock.instant());
        llamadas.add(llamada);
        log.info("[FakeBpm] {}", llamada);
    }

    public record LlamadaBpm(String operacion, String username, Map<String, Object> argumentos, Instant instante) {
    }

    private static final class CasoFake {
        final String caseId;
        final Map<String, Object> variables = new ConcurrentHashMap<>();

        CasoFake(String caseId) {
            this.caseId = caseId;
        }
    }

    private record TareaFake(String taskId, String nombre, String caseId, Rol actor, Instant disponibleDesde) {
    }
}
