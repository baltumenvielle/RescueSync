package ar.edu.unlp.dssd.rescuesync.oferta.dto;

import ar.edu.unlp.dssd.rescuesync.lote.TipoLote;
import ar.edu.unlp.dssd.rescuesync.oferta.Oferta;
import java.util.List;

/** Oferta en el formato consolidado que se entrega al BPM / Sistema Nacional. */
public record OfertaConsolidadaDto(Long ofertaId, Long ongId, String ongNombre, String ongCuit, int version,
                                   List<Item> items) {

    public record Item(Long loteId, TipoLote tipo, int cantidad) {
    }

    public static OfertaConsolidadaDto de(Oferta o) {
        return new OfertaConsolidadaDto(o.getId(), o.getOngLider().getId(), o.getOngLider().getNombre(),
                o.getOngLider().getCuit(), o.getVersionActual(),
                o.versionVigente().getItems().stream()
                        .map(i -> new Item(i.getLote().getId(), i.getLote().getTipo(), i.getCantidadOfrecida()))
                        .toList());
    }
}
