package ar.edu.unlp.dssd.rescuesync.internal;

import ar.edu.unlp.dssd.rescuesync.cobertura.ResultadoCobertura;
import ar.edu.unlp.dssd.rescuesync.emergencia.ConvocatoriaService;
import ar.edu.unlp.dssd.rescuesync.emergencia.dto.PublicacionDto;
import ar.edu.unlp.dssd.rescuesync.oferta.OfertaService;
import ar.edu.unlp.dssd.rescuesync.oferta.dto.OfertaConsolidadaDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints invocados por los conectores REST de las tareas automáticas del proceso en Bonita.
 * Protegidos con el header X-Internal-Token. Las respuestas son JSON plano para poder mapear
 * {@code bodyAsObject.<clave>} a variables de proceso.
 */
@RestController
@RequestMapping("/internal/bpm/emergencias/{id}")
@SecurityRequirement(name = "internal")
public class InternalBpmController {

    private final ConvocatoriaService convocatorias;
    private final OfertaService ofertas;

    public InternalBpmController(ConvocatoriaService convocatorias, OfertaService ofertas) {
        this.convocatorias = convocatorias;
        this.ofertas = ofertas;
    }

    @Operation(summary = "Publicación de convocatoria",
            description = "Abre la ventana de ofertas usando las horas definidas por el CCR. Idempotente.")
    @PostMapping("/convocatoria/publicar")
    PublicacionDto publicar(@PathVariable Long id) {
        return convocatorias.publicar(id);
    }

    @Operation(summary = "Evaluación de cobertura",
            description = "Se invoca al vencer el timer. Cierra la convocatoria si seguía abierta (idempotente) "
                    + "y devuelve {completa} para mapear a la variable coberturaCompleta.")
    @GetMapping("/cobertura")
    CoberturaBpmDto cobertura(@PathVariable Long id) {
        ResultadoCobertura r = convocatorias.cerrarYEvaluar(id);
        return new CoberturaBpmDto(id, r.completa(), r.lotesTotales(), r.lotesCubiertos());
    }

    @Operation(summary = "Listado consolidado de ofertas",
            description = "Ofertas enviadas en su versión vigente, para la validación externa.")
    @GetMapping("/ofertas")
    OfertasBpmDto ofertas(@PathVariable Long id) {
        List<OfertaConsolidadaDto> lista = ofertas.consolidadas(id);
        return new OfertasBpmDto(id, lista.size(), lista);
    }

    record CoberturaBpmDto(Long emergenciaId, boolean completa, int lotesTotales, int lotesCubiertos) {
    }

    record OfertasBpmDto(Long emergenciaId, int cantidad, List<OfertaConsolidadaDto> ofertas) {
    }
}
