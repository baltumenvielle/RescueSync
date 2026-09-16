package ar.edu.unlp.dssd.rescuesync.lote.dto;

import ar.edu.unlp.dssd.rescuesync.lote.Lote;
import ar.edu.unlp.dssd.rescuesync.lote.Prioridad;
import ar.edu.unlp.dssd.rescuesync.lote.TipoLote;

public record LoteDto(Long id, TipoLote tipo, String descripcion, String unidad, int cantidadRequerida,
                      Prioridad prioridad) {

    public static LoteDto de(Lote l) {
        return new LoteDto(l.getId(), l.getTipo(), l.getDescripcion(), l.getUnidad(), l.getCantidadRequerida(),
                l.getPrioridad());
    }
}
