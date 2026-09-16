package ar.edu.unlp.dssd.rescuesync.cobertura;

import ar.edu.unlp.dssd.rescuesync.lote.TipoLote;

public record CoberturaLote(Long loteId, TipoLote tipo, String descripcion, String unidad, int cantidadRequerida,
                            int cantidadOfrecida, int porcentaje, boolean cubierto, int ofertasQueAportan) {
}
