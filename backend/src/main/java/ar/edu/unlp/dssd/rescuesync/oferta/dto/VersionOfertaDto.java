package ar.edu.unlp.dssd.rescuesync.oferta.dto;

import ar.edu.unlp.dssd.rescuesync.oferta.OfertaVersion;
import java.time.Instant;
import java.util.List;

public record VersionOfertaDto(int numero, String autor, String comentario, Instant createdAt,
                               List<ItemOfertaDto> items) {

    public static VersionOfertaDto de(OfertaVersion v) {
        return new VersionOfertaDto(v.getNumero(), v.getAutor().getNombre(), v.getComentario(), v.getCreatedAt(),
                v.getItems().stream().map(ItemOfertaDto::de).toList());
    }
}
