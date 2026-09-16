package ar.edu.unlp.dssd.rescuesync.oferta.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OfertaRequest(
        @Size(max = 2000, message = "El comentario admite hasta 2000 caracteres") String comentario,
        @NotEmpty(message = "La oferta debe incluir al menos un lote") List<@Valid @NotNull Item> items) {

    public record Item(
            @NotNull(message = "Indique el lote") Long loteId,
            @NotNull(message = "Indique la cantidad")
            @Min(value = 1, message = "La cantidad debe ser mayor a cero")
            @Max(value = 10_000_000, message = "Cantidad fuera de rango") Integer cantidad) {
    }
}
