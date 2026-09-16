package ar.edu.unlp.dssd.rescuesync.emergencia;

import ar.edu.unlp.dssd.rescuesync.common.AccesoDenegadoException;
import ar.edu.unlp.dssd.rescuesync.common.NoEncontradoException;
import ar.edu.unlp.dssd.rescuesync.seguridad.UsuarioActual;
import org.springframework.stereotype.Component;

/** Alcance por organización sobre las emergencias. */
@Component
public class AccesoEmergencias {

    private final EmergenciaRepository repository;

    public AccesoEmergencias(EmergenciaRepository repository) {
        this.repository = repository;
    }

    public Emergencia visible(Long id, UsuarioActual usuario) {
        Emergencia emergencia = repository.findDetalleById(id)
                .orElseThrow(() -> new NoEncontradoException("Emergencia", id));
        exigirVisible(emergencia, usuario);
        return emergencia;
    }

    public void exigirVisible(Emergencia emergencia, UsuarioActual usuario) {
        boolean visible = switch (usuario.rol()) {
            case CCR, AUDITOR -> true;
            case OPERADOR_MUNICIPAL -> emergencia.getMunicipio().getId().equals(usuario.organizacionId());
            case REPRESENTANTE_ONG -> emergencia.getEstado().esPublica();
        };
        if (!visible) {
            throw new AccesoDenegadoException("Emergencia fuera del alcance del usuario");
        }
    }
}
