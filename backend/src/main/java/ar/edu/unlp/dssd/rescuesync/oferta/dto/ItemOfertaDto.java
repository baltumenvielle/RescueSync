package ar.edu.unlp.dssd.rescuesync.oferta.dto;

import ar.edu.unlp.dssd.rescuesync.lote.TipoLote;
import ar.edu.unlp.dssd.rescuesync.oferta.OfertaItem;
import ar.edu.unlp.dssd.rescuesync.oferta.RolItem;

public record ItemOfertaDto(Long loteId, TipoLote tipo, String descripcionLote, String unidad,
                            int cantidadRequerida, int cantidadOfrecida, RolItem rol) {

    public static ItemOfertaDto de(OfertaItem i) {
        return new ItemOfertaDto(i.getLote().getId(), i.getLote().getTipo(), i.getLote().getDescripcion(),
                i.getLote().getUnidad(), i.getLote().getCantidadRequerida(), i.getCantidadOfrecida(), i.getRol());
    }
}
