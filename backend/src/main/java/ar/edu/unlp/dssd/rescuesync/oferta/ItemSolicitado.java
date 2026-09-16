package ar.edu.unlp.dssd.rescuesync.oferta;

import ar.edu.unlp.dssd.rescuesync.lote.Lote;

/** Cantidad que una ONG ofrece para un lote al crear o editar una oferta. */
public record ItemSolicitado(Lote lote, int cantidad) {
}
