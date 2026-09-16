package ar.edu.unlp.dssd.rescuesync.oferta;

import ar.edu.unlp.dssd.rescuesync.oferta.dto.ConvocatoriaDto;
import ar.edu.unlp.dssd.rescuesync.oferta.dto.OfertaDto;
import ar.edu.unlp.dssd.rescuesync.oferta.dto.OfertaRequest;
import ar.edu.unlp.dssd.rescuesync.oferta.dto.VersionOfertaDto;
import ar.edu.unlp.dssd.rescuesync.seguridad.SesionActual;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OfertaController {

    private final OfertaService ofertas;
    private final SesionActual sesion;

    public OfertaController(OfertaService ofertas, SesionActual sesion) {
        this.ofertas = ofertas;
        this.sesion = sesion;
    }

    @GetMapping("/api/ong/convocatorias")
    @PreAuthorize("hasRole('REPRESENTANTE_ONG')")
    List<ConvocatoriaDto> convocatorias() {
        return ofertas.convocatoriasAbiertas(sesion.usuario());
    }

    @GetMapping("/api/ong/ofertas")
    @PreAuthorize("hasRole('REPRESENTANTE_ONG')")
    List<OfertaDto> misOfertas() {
        return ofertas.misOfertas(sesion.usuario());
    }

    @PostMapping("/api/emergencias/{emergenciaId}/ofertas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('REPRESENTANTE_ONG')")
    OfertaDto crear(@PathVariable Long emergenciaId, @Valid @RequestBody OfertaRequest req) {
        return ofertas.crear(sesion.usuario(), emergenciaId, req);
    }

    @GetMapping("/api/emergencias/{emergenciaId}/ofertas")
    @PreAuthorize("hasAnyRole('CCR', 'AUDITOR')")
    List<OfertaDto> deEmergencia(@PathVariable Long emergenciaId) {
        return ofertas.deEmergencia(sesion.usuario(), emergenciaId);
    }

    @GetMapping("/api/ofertas/{ofertaId}")
    @PreAuthorize("hasAnyRole('REPRESENTANTE_ONG', 'CCR', 'AUDITOR')")
    OfertaDto detalle(@PathVariable Long ofertaId) {
        return ofertas.detalle(sesion.usuario(), ofertaId);
    }

    /** Edita la oferta generando una nueva versión. */
    @PutMapping("/api/ofertas/{ofertaId}")
    @PreAuthorize("hasRole('REPRESENTANTE_ONG')")
    OfertaDto editar(@PathVariable Long ofertaId, @Valid @RequestBody OfertaRequest req) {
        return ofertas.editar(sesion.usuario(), ofertaId, req);
    }

    @GetMapping("/api/ofertas/{ofertaId}/versiones")
    @PreAuthorize("hasAnyRole('REPRESENTANTE_ONG', 'CCR', 'AUDITOR')")
    List<VersionOfertaDto> versiones(@PathVariable Long ofertaId) {
        return ofertas.versiones(sesion.usuario(), ofertaId);
    }

    @PostMapping("/api/ofertas/{ofertaId}/enviar")
    @PreAuthorize("hasRole('REPRESENTANTE_ONG')")
    OfertaDto enviar(@PathVariable Long ofertaId) {
        return ofertas.enviar(sesion.usuario(), ofertaId);
    }
}
