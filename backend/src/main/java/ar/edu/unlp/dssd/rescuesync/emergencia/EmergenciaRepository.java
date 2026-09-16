package ar.edu.unlp.dssd.rescuesync.emergencia;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

public interface EmergenciaRepository extends JpaRepository<Emergencia, Long> {

    @EntityGraph(attributePaths = "municipio")
    List<Emergencia> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "municipio")
    List<Emergencia> findByMunicipioIdOrderByCreatedAtDesc(Long municipioId);

    @EntityGraph(attributePaths = "municipio")
    List<Emergencia> findByEstadoAndFechaCierreConvocatoriaAfterOrderByFechaCierreConvocatoriaAsc(
            EstadoEmergencia estado, Instant ahora);

    @EntityGraph(attributePaths = "municipio")
    List<Emergencia> findByIdIn(Collection<Long> ids);

    @EntityGraph(attributePaths = {"municipio", "revisadaPor"})
    Optional<Emergencia> findDetalleById(Long id);

    Optional<Emergencia> findByCaseIdBonita(String caseIdBonita);

    @EntityGraph(attributePaths = "municipio")
    List<Emergencia> findByCaseIdBonitaIn(Collection<String> casos);

    /**
     * Bloqueo pesimista de la emergencia: serializa las operaciones sobre ofertas contra
     * el cierre de la ventana.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Emergencia e where e.id = :id")
    Optional<Emergencia> findParaActualizar(Long id);
}
