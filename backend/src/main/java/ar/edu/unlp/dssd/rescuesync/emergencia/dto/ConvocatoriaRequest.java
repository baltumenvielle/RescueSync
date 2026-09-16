package ar.edu.unlp.dssd.rescuesync.emergencia.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ConvocatoriaRequest(
        @NotNull(message = "Indique la duración de la ventana")
        @Min(value = 1, message = "La ventana debe durar al menos 1 hora")
        @Max(value = 720, message = "La ventana no puede superar las 720 horas (30 días)") Integer horasVentana) {
}
