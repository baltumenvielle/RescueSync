package ar.edu.unlp.dssd.rescuesync.usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = "organizacion")
    Optional<Usuario> findByUsername(String username);

    @EntityGraph(attributePaths = "organizacion")
    Optional<Usuario> findWithOrganizacionById(Long id);

    boolean existsByUsernameIgnoreCase(String username);
}
