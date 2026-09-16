package ar.edu.unlp.dssd.rescuesync.cobertura;

import ar.edu.unlp.dssd.rescuesync.lote.Lote;
import ar.edu.unlp.dssd.rescuesync.oferta.EstadoOferta;
import ar.edu.unlp.dssd.rescuesync.oferta.Oferta;
import ar.edu.unlp.dssd.rescuesync.oferta.OfertaVersion;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Cobertura de los lotes de una emergencia: para cada lote se suma la cantidad ofrecida en la
 * versión vigente de las ofertas ENVIADAS. Es completa si todos los lotes alcanzan o superan
 * la cantidad requerida.
 */
public final class CalculadoraCobertura {

    private CalculadoraCobertura() {
    }

    public static ResultadoCobertura calcular(Collection<Lote> lotes, Collection<Oferta> ofertas) {
        List<OfertaVersion> vigentes = ofertas.stream()
                .filter(o -> o.getEstado() == EstadoOferta.ENVIADA)
                .map(Oferta::versionVigente)
                .toList();

        List<CoberturaLote> detalle = new ArrayList<>();
        int cubiertos = 0;
        for (Lote lote : lotes) {
            int ofrecida = 0;
            int aportantes = 0;
            for (OfertaVersion version : vigentes) {
                int cantidad = version.cantidadPara(lote);
                if (cantidad > 0) {
                    ofrecida += cantidad;
                    aportantes++;
                }
            }
            boolean cubierto = ofrecida >= lote.getCantidadRequerida();
            if (cubierto) {
                cubiertos++;
            }
            int porcentaje = (int) Math.min(100, Math.floor(ofrecida * 100.0 / lote.getCantidadRequerida()));
            detalle.add(new CoberturaLote(lote.getId(), lote.getTipo(), lote.getDescripcion(), lote.getUnidad(),
                    lote.getCantidadRequerida(), ofrecida, porcentaje, cubierto, aportantes));
        }
        boolean completa = !lotes.isEmpty() && cubiertos == lotes.size();
        return new ResultadoCobertura(completa, lotes.size(), cubiertos, detalle);
    }
}
