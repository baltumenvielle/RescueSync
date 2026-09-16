package ar.edu.unlp.dssd.rescuesync.lote;

import ar.edu.unlp.dssd.rescuesync.lote.dto.LoteDto;
import ar.edu.unlp.dssd.rescuesync.lote.dto.LoteRequest;
import ar.edu.unlp.dssd.rescuesync.seguridad.SesionActual;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emergencias/{emergenciaId}/lotes")
public class LoteController {

    private final LoteService lotes;
    private final SesionActual sesion;

    public LoteController(LoteService lotes, SesionActual sesion) {
        this.lotes = lotes;
        this.sesion = sesion;
    }

    @GetMapping
    List<LoteDto> listar(@PathVariable Long emergenciaId) {
        return lotes.listar(sesion.usuario(), emergenciaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CCR')")
    LoteDto crear(@PathVariable Long emergenciaId, @Valid @RequestBody LoteRequest req) {
        return lotes.crear(sesion.usuario(), emergenciaId, req);
    }

    @PutMapping("/{loteId}")
    @PreAuthorize("hasRole('CCR')")
    LoteDto actualizar(@PathVariable Long emergenciaId, @PathVariable Long loteId,
                       @Valid @RequestBody LoteRequest req) {
        return lotes.actualizar(sesion.usuario(), emergenciaId, loteId, req);
    }

    @DeleteMapping("/{loteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CCR')")
    void eliminar(@PathVariable Long emergenciaId, @PathVariable Long loteId) {
        lotes.eliminar(sesion.usuario(), emergenciaId, loteId);
    }
}
