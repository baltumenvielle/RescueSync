package ar.edu.unlp.dssd.rescuesync.bpm;

import java.time.Instant;

/**
 * Tarea humana disponible en el motor, expresada en términos del dominio.
 *
 * @param emergenciaId puede ser nulo si la implementación no lo lee del caso; la app lo
 *                     resuelve por {@code caseId}
 */
public record TareaPendiente(String taskId, String nombreTarea, String caseId, Long emergenciaId,
                             Instant fechaDisponible) {
}
