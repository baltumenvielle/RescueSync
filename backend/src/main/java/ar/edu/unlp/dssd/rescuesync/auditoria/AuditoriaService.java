package ar.edu.unlp.dssd.rescuesync.auditoria;

import java.time.Clock;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/** Registro de eventos relevantes del dominio para trazabilidad. */
@Service
public class AuditoriaService {

    private final EventoAuditoriaRepository repository;
    private final JsonMapper jsonMapper;
    private final Clock clock;

    public AuditoriaService(EventoAuditoriaRepository repository, JsonMapper jsonMapper, Clock clock) {
        this.repository = repository;
        this.jsonMapper = jsonMapper;
        this.clock = clock;
    }

    /** Se registra dentro de la transacción de la operación auditada: si ésta falla, el evento no queda. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(String entidad, Long entidadId, String accion, Long usuarioId, Map<String, ?> datos) {
        String payload = datos == null ? null : jsonMapper.writeValueAsString(datos);
        repository.save(new EventoAuditoria(entidad, entidadId, accion, usuarioId, payload, clock.instant()));
    }
}
