package ar.edu.unlp.dssd.rescuesync.lote.dto;

import ar.edu.unlp.dssd.rescuesync.lote.Prioridad;
import ar.edu.unlp.dssd.rescuesync.lote.TipoLote;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoteRequest(
        @NotNull(message = "Indique si el lote es de personal o de recursos") TipoLote tipo,
        @NotBlank(message = "Describa el lote")
        @Size(max = 300, message = "La descripción admite hasta 300 caracteres") String descripcion,
        @NotBlank(message = "Indique la unidad de medida")
        @Size(max = 50, message = "La unidad admite hasta 50 caracteres") String unidad,
        @NotNull(message = "Indique la cantidad requerida")
        @Min(value = 1, message = "La cantidad debe ser mayor a cero")
        @Max(value = 10_000_000, message = "Cantidad fuera de rango") Integer cantidadRequerida,
        @NotNull(message = "Indique la prioridad") Prioridad prioridad) {
}
