package ar.edu.unlp.dssd.rescuesync.bpm;

import ar.edu.unlp.dssd.rescuesync.common.ReglaNegocioException;
import ar.edu.unlp.dssd.rescuesync.emergencia.Emergencia;
import ar.edu.unlp.dssd.rescuesync.emergencia.EmergenciaRepository;
import ar.edu.unlp.dssd.rescuesync.emergencia.dto.EmergenciaResumenDto;
import ar.edu.unlp.dssd.rescuesync.usuario.Rol;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Bandeja de tareas y ejecución de tareas humanas a través de {@link BpmPort}. */
@Service
public class TareaService {

    private static final Logger log = LoggerFactory.getLogger(TareaService.class);

    private final BpmPort bpm;
    private final NombresTareas nombres;
    private final EmergenciaRepository emergencias;

    public TareaService(BpmPort bpm, NombresTareas nombres, EmergenciaRepository emergencias) {
        this.bpm = bpm;
        this.nombres = nombres;
        this.emergencias = emergencias;
    }

    /** Tareas del usuario enriquecidas con la emergencia, respetando el alcance de su organización. */
    @Transactional(readOnly = true)
    public List<TareaDto> bandeja(UsuarioBpm usuario, Long organizacionId) {
        List<TareaPendiente> tareas = bpm.tareasDe(usuario);
        Map<String, Emergencia> porCaso = emergenciasDe(tareas);
        return tareas.stream()
                .map(t -> {
                    Emergencia e = porCaso.get(t.caseId());
                    return new TareaDto(t.taskId(), t.nombreTarea(), nombres.accionDe(t.nombreTarea()), t.caseId(),
                            e != null ? e.getId() : t.emergenciaId(), t.fechaDisponible(),
                            e != null ? EmergenciaResumenDto.de(e) : null);
                })
                .filter(t -> usuario.rol() != Rol.OPERADOR_MUNICIPAL
                        || (t.emergencia() != null && t.emergencia().municipio().id().equals(organizacionId)))
                .sorted(Comparator.comparing(TareaDto::fechaDisponible, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    /** Busca la tarea del caso; falla si el motor no la tiene disponible para el usuario. */
    public TareaPendiente exigirTarea(UsuarioBpm usuario, Emergencia emergencia, AccionTarea accion) {
        return buscarTarea(usuario, emergencia.getCaseIdBonita(), accion)
                .orElseThrow(() -> new ReglaNegocioException("La tarea \"" + nombres.nombreDe(accion)
                        + "\" no está disponible en el proceso para la emergencia " + emergencia.getId()));
    }

    public void completar(UsuarioBpm usuario, TareaPendiente tarea) {
        bpm.completarTarea(usuario, tarea.taskId());
    }

    /** Completa la tarea si está disponible; si no, solo deja constancia en el log. */
    public boolean completarSiDisponible(UsuarioBpm usuario, String caseId, AccionTarea accion) {
        Optional<TareaPendiente> tarea = buscarTarea(usuario, caseId, accion);
        if (tarea.isEmpty()) {
            log.warn("Tarea '{}' no disponible para el caso {} (usuario {})", nombres.nombreDe(accion), caseId,
                    usuario.username());
            return false;
        }
        bpm.completarTarea(usuario, tarea.get().taskId());
        return true;
    }

    private Optional<TareaPendiente> buscarTarea(UsuarioBpm usuario, String caseId, AccionTarea accion) {
        if (caseId == null) {
            throw new ReglaNegocioException("La emergencia no tiene un caso de proceso asociado");
        }
        return bpm.tareasDe(usuario).stream()
                .filter(t -> caseId.equals(t.caseId()) && nombres.es(t.nombreTarea(), accion))
                .findFirst();
    }

    private Map<String, Emergencia> emergenciasDe(List<TareaPendiente> tareas) {
        List<String> casos = tareas.stream().map(TareaPendiente::caseId).filter(Objects::nonNull).distinct().toList();
        if (casos.isEmpty()) {
            return Map.of();
        }
        return emergencias.findByCaseIdBonitaIn(casos).stream()
                .collect(Collectors.toMap(Emergencia::getCaseIdBonita, Function.identity()));
    }
}
