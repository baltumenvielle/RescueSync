package ar.edu.unlp.dssd.rescuesync.bpm;

import ar.edu.unlp.dssd.rescuesync.emergencia.dto.EmergenciaResumenDto;
import java.time.Instant;

public record TareaDto(String taskId, String nombreTarea, AccionTarea accion, String caseId, Long emergenciaId,
                       Instant fechaDisponible, EmergenciaResumenDto emergencia) {
}
