package ar.edu.unlp.dssd.rescuesync.bpm;

import ar.edu.unlp.dssd.rescuesync.seguridad.SesionActual;
import ar.edu.unlp.dssd.rescuesync.seguridad.UsuarioActual;
import ar.edu.unlp.dssd.rescuesync.usuario.UsuarioService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tareas")
public class TareaController {

    private final TareaService tareas;
    private final UsuarioService usuarios;
    private final SesionActual sesion;

    public TareaController(TareaService tareas, UsuarioService usuarios, SesionActual sesion) {
        this.tareas = tareas;
        this.usuarios = usuarios;
        this.sesion = sesion;
    }

    /** Bandeja de tareas pendientes del usuario. El auditor no participa del proceso. */
    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR_MUNICIPAL', 'CCR', 'REPRESENTANTE_ONG')")
    List<TareaDto> bandeja() {
        UsuarioActual actual = sesion.usuario();
        return tareas.bandeja(UsuarioBpm.de(usuarios.obtener(actual.id())), actual.organizacionId());
    }
}
