package ar.edu.unlp.dssd.rescuesync.organizacion;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizacionRepository extends JpaRepository<Organizacion, Long> {

    List<Organizacion> findAllByOrderByTipoAscNombreAsc();

    boolean existsByNombreIgnoreCaseAndTipo(String nombre, TipoOrganizacion tipo);
}
