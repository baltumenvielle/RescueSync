package ar.edu.unlp.dssd.rescuesync.auditoria;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auditoria")
@PreAuthorize("hasRole('AUDITOR')")
public class AuditoriaController {

    private final EventoAuditoriaRepository repository;

    public AuditoriaController(EventoAuditoriaRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/eventos")
    PaginaEventos eventos(@RequestParam(required = false) String entidad,
                          @RequestParam(required = false) Long entidadId,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "50") int size) {
        Page<EventoAuditoria> pagina = repository.buscar(entidad, entidadId,
                PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 200)));
        return new PaginaEventos(
                pagina.getContent().stream().map(EventoDto::de).toList(),
                pagina.getNumber(), pagina.getSize(), pagina.getTotalElements());
    }

    record PaginaEventos(List<EventoDto> contenido, int pagina, int tamanio, long total) {
    }

    record EventoDto(Long id, String entidad, Long entidadId, String accion, Long usuarioId, String payload,
                     Instant timestamp) {
        static EventoDto de(EventoAuditoria e) {
            return new EventoDto(e.getId(), e.getEntidad(), e.getEntidadId(), e.getAccion(), e.getUsuarioId(),
                    e.getPayload(), e.getTimestamp());
        }
    }
}
