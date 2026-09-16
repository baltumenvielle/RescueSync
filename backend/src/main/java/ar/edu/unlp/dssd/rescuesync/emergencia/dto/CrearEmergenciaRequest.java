package ar.edu.unlp.dssd.rescuesync.emergencia.dto;

import ar.edu.unlp.dssd.rescuesync.emergencia.Gravedad;
import ar.edu.unlp.dssd.rescuesync.emergencia.TipoDesastre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearEmergenciaRequest(
        @NotNull(message = "Indique el tipo de desastre") TipoDesastre tipoDesastre,
        @NotNull(message = "Indique la gravedad") Gravedad gravedad,
        @NotBlank(message = "Indique la zona afectada")
        @Size(max = 300, message = "La zona afectada admite hasta 300 caracteres") String zonaAfectada,
        @NotBlank(message = "Describa la emergencia")
        @Size(min = 20, max = 4000, message = "La descripción debe tener entre 20 y 4000 caracteres")
        String descripcion) {
}
