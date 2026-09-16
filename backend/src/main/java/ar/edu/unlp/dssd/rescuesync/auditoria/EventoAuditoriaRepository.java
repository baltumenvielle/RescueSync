package ar.edu.unlp.dssd.rescuesync.auditoria;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoAuditoriaRepository extends JpaRepository<EventoAuditoria, Long> {

    Page<EventoAuditoria> findAllByOrderByTimestampDescIdDesc(Pageable pageable);

    Page<EventoAuditoria> findByEntidadOrderByTimestampDescIdDesc(String entidad, Pageable pageable);

    Page<EventoAuditoria> findByEntidadAndEntidadIdOrderByTimestampDescIdDesc(String entidad, Long entidadId,
                                                                             Pageable pageable);

    default Page<EventoAuditoria> buscar(String entidad, Long entidadId, Pageable pageable) {
        if (entidad == null || entidad.isBlank()) {
            return findAllByOrderByTimestampDescIdDesc(pageable);
        }
        return entidadId == null
                ? findByEntidadOrderByTimestampDescIdDesc(entidad, pageable)
                : findByEntidadAndEntidadIdOrderByTimestampDescIdDesc(entidad, entidadId, pageable);
    }
}
