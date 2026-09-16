package ar.edu.unlp.dssd.rescuesync.emergencia.dto;

import ar.edu.unlp.dssd.rescuesync.emergencia.DecisionCcr;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** @param horasVentana opcional; solo se usa con REABRIR para redefinir la duración de la nueva ventana */
public record DecisionCoberturaRequest(
        @NotNull(message = "Indique la decisión") DecisionCcr decision,
        @Min(value = 1, message = "La ventana debe durar al menos 1 hora")
        @Max(value = 720, message = "La ventana no puede superar las 720 horas (30 días)") Integer horasVentana) {
}
