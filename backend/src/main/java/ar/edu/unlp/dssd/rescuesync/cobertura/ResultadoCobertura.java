package ar.edu.unlp.dssd.rescuesync.cobertura;

import java.util.List;

public record ResultadoCobertura(boolean completa, int lotesTotales, int lotesCubiertos, List<CoberturaLote> lotes) {
}
