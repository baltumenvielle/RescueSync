package ar.edu.unlp.dssd.rescuesync.auth.registro;

import ar.edu.unlp.dssd.rescuesync.auditoria.AuditoriaService;
import ar.edu.unlp.dssd.rescuesync.common.CampoInvalidoException;
import ar.edu.unlp.dssd.rescuesync.organizacion.Organizacion;
import ar.edu.unlp.dssd.rescuesync.organizacion.OrganizacionDto;
import ar.edu.unlp.dssd.rescuesync.organizacion.OrganizacionRepository;
import ar.edu.unlp.dssd.rescuesync.usuario.Usuario;
import ar.edu.unlp.dssd.rescuesync.usuario.UsuarioRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroDebugService {

    private final UsuarioRepository usuarios;
    private final OrganizacionRepository organizaciones;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoria;

    public RegistroDebugService(UsuarioRepository usuarios, OrganizacionRepository organizaciones,
                                PasswordEncoder passwordEncoder, AuditoriaService auditoria) {
        this.usuarios = usuarios;
        this.organizaciones = organizaciones;
        this.passwordEncoder = passwordEncoder;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<OrganizacionDto> organizaciones() {
        return organizaciones.findAllByOrderByTipoAscNombreAsc().stream().map(OrganizacionDto::de).toList();
    }

    @Transactional
    public void registrar(RegistroDebugRequest req) {
        String username = req.username().trim();
        if (usuarios.existsByUsernameIgnoreCase(username)) {
            throw new CampoInvalidoException("username", "El nombre de usuario ya está en uso");
        }
        Organizacion organizacion = organizacionPara(req);
        Usuario usuario = usuarios.save(new Usuario(username, passwordEncoder.encode(req.password()),
                req.email().trim(), req.nombre().trim(), req.rol(), organizacion));

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("username", username);
        datos.put("rol", req.rol());
        datos.put("organizacionId", organizacion.getId());
        datos.put("organizacionNueva", req.organizacionId() == null);
        auditoria.registrar("USUARIO", usuario.getId(), "REGISTRADO_DEBUG", usuario.getId(), datos);
    }

    private Organizacion organizacionPara(RegistroDebugRequest req) {
        if (req.organizacionId() != null) {
            Organizacion existente = organizaciones.findById(req.organizacionId())
                    .orElseThrow(() -> new CampoInvalidoException("organizacionId", "La organización no existe"));
            if (existente.getTipo() != req.rol().tipoOrganizacion()) {
                throw new CampoInvalidoException("organizacionId",
                        "Un usuario con ese perfil debe pertenecer a una organización de tipo "
                                + req.rol().tipoOrganizacion());
            }
            return existente;
        }
        String nombre = req.nuevaOrganizacion().trim();
        if (organizaciones.existsByNombreIgnoreCaseAndTipo(nombre, req.rol().tipoOrganizacion())) {
            throw new CampoInvalidoException("nuevaOrganizacion",
                    "Ya existe una organización con ese nombre; elíjala de la lista");
        }
        return organizaciones.save(new Organizacion(req.rol().tipoOrganizacion(), nombre, null));
    }
}
