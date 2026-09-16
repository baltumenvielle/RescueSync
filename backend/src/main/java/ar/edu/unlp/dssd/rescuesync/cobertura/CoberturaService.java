package ar.edu.unlp.dssd.rescuesync.cobertura;

import ar.edu.unlp.dssd.rescuesync.lote.LoteRepository;
import ar.edu.unlp.dssd.rescuesync.oferta.EstadoOferta;
import ar.edu.unlp.dssd.rescuesync.oferta.OfertaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CoberturaService {

    private final LoteRepository lotes;
    private final OfertaRepository ofertas;

    public CoberturaService(LoteRepository lotes, OfertaRepository ofertas) {
        this.lotes = lotes;
        this.ofertas = ofertas;
    }

    @Transactional(readOnly = true)
    public ResultadoCobertura calcular(Long emergenciaId) {
        return CalculadoraCobertura.calcular(
                lotes.findByEmergenciaIdOrderById(emergenciaId),
                ofertas.findByEmergenciaIdAndEstadoIn(emergenciaId, List.of(EstadoOferta.ENVIADA)));
    }
}
