package ar.edu.unlp.dssd.rescuesync.emergencia.dto;

import java.time.Instant;

/** Respuesta a los conectores de Bonita: JSON plano con valores simples. */
public record PublicacionDto(Long emergenciaId, boolean publicada, int horasVentana, Instant fechaApertura,
                             Instant fechaCierre) {
}
