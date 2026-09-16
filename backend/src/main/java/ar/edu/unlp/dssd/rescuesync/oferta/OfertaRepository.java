package ar.edu.unlp.dssd.rescuesync.oferta;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    @EntityGraph(attributePaths = {"ongLider", "emergencia"})
    List<Oferta> findByEmergenciaIdOrderById(Long emergenciaId);

    @EntityGraph(attributePaths = {"ongLider", "emergencia"})
    List<Oferta> findByEmergenciaIdAndEstadoIn(Long emergenciaId, Collection<EstadoOferta> estados);

    @EntityGraph(attributePaths = {"ongLider", "emergencia", "emergencia.municipio"})
    List<Oferta> findByOngLiderIdOrderByCreatedAtDesc(Long ongId);

    @EntityGraph(attributePaths = {"ongLider", "emergencia"})
    Optional<Oferta> findDetalleById(Long id);

    Optional<Oferta> findByEmergenciaIdAndOngLiderId(Long emergenciaId, Long ongId);

    boolean existsByEmergenciaIdAndOngLiderId(Long emergenciaId, Long ongId);
}
