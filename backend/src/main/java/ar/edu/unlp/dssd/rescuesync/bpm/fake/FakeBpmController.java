package ar.edu.unlp.dssd.rescuesync.bpm.fake;

import ar.edu.unlp.dssd.rescuesync.cobertura.ResultadoCobertura;
import ar.edu.unlp.dssd.rescuesync.common.NoEncontradoException;
import ar.edu.unlp.dssd.rescuesync.emergencia.ConvocatoriaService;
import ar.edu.unlp.dssd.rescuesync.emergencia.Emergencia;
import ar.edu.unlp.dssd.rescuesync.emergencia.EmergenciaRepository;
import ar.edu.unlp.dssd.rescuesync.emergencia.dto.PublicacionDto;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Solo sin Bonita: permite disparar desde la SPA lo que en el motor real hacen solos la tarea
 * automática de publicación y el timer de la ventana. Invoca los mismos servicios que los
 * endpoints /internal/bpm/**.
 */
@RestController
@Profile("!bonita")
@RequestMapping("/api/dev/bpm")
@PreAuthorize("hasRole('CCR')")
public class FakeBpmController {

    private final FakeBpmAdapter fake;
    private final ConvocatoriaService convocatorias;
    private final EmergenciaRepository emergencias;

    public FakeBpmController(FakeBpmAdapter fake, ConvocatoriaService convocatorias,
                             EmergenciaRepository emergencias) {
        this.fake = fake;
        this.convocatorias = convocatorias;
        this.emergencias = emergencias;
    }

    @GetMapping
    Map<String, Object> estado() {
        return Map.of("modo", "FAKE", "llamadasRegistradas", fake.llamadas().size());
    }

    @PostMapping("/emergencias/{id}/publicar")
    PublicacionDto publicar(@PathVariable Long id) {
        return convocatorias.publicar(id);
    }

    @PostMapping("/emergencias/{id}/fin-ventana")
    ResultadoCobertura finVentana(@PathVariable Long id) {
        Emergencia emergencia = emergencias.findById(id).orElseThrow(() -> new NoEncontradoException("Emergencia", id));
        ResultadoCobertura resultado = convocatorias.cerrarYEvaluar(id);
        fake.simularFinVentana(emergencia.getCaseIdBonita(), resultado.completa());
        return resultado;
    }
}
