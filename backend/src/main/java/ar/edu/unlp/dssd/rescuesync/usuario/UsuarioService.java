package ar.edu.unlp.dssd.rescuesync.usuario;

import ar.edu.unlp.dssd.rescuesync.common.NoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return repository.findWithOrganizacionById(id).orElseThrow(() -> new NoEncontradoException("Usuario", id));
    }
}
