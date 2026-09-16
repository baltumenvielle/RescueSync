package ar.edu.unlp.dssd.rescuesync.lote;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findByEmergenciaIdOrderById(Long emergenciaId);

    long countByEmergenciaId(Long emergenciaId);

    Optional<Lote> findByIdAndEmergenciaId(Long id, Long emergenciaId);

    @Query("select count(i) > 0 from OfertaItem i where i.lote.id = :loteId")
    boolean estaReferenciadoEnOfertas(Long loteId);
}
