package ar.edu.unlp.dssd.rescuesync.auth;

import ar.edu.unlp.dssd.rescuesync.bpm.BpmPort;
import ar.edu.unlp.dssd.rescuesync.bpm.UsuarioBpm;
import ar.edu.unlp.dssd.rescuesync.seguridad.JwtService;
import ar.edu.unlp.dssd.rescuesync.usuario.Rol;
import ar.edu.unlp.dssd.rescuesync.usuario.Usuario;
import ar.edu.unlp.dssd.rescuesync.usuario.UsuarioRepository;
import ar.edu.unlp.dssd.rescuesync.usuario.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    // se calcula el hfash de una contraseña cualquiera para tener el mismo tiempo de respuesta que con usuarios inexistentes
    private static final String HASH_FICTICIO = "$2a$10$Zi7iqrBVSb95PBz4ztgAduy/bxxByab/.e4s088sPhbltAOuiExWa";

    private final UsuarioRepository usuarios;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwt;
    private final BpmPort bpm;

    public AuthService(UsuarioRepository usuarios, UsuarioService usuarioService, PasswordEncoder passwordEncoder,
                       JwtService jwt, BpmPort bpm) {
        this.usuarios = usuarios;
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
        this.bpm = bpm;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        Usuario usuario = usuarios.findByUsername(req.username().trim()).orElse(null);
        String hash = usuario != null ? usuario.getPasswordHash() : HASH_FICTICIO;
        boolean coincide = passwordEncoder.matches(req.password(), hash);
        if (usuario == null || !coincide || !usuario.isActivo()) {
            throw new CredencialesInvalidasException();
        }
        // el auditor no participa del proceso: no necesita sesión en el motor.
        if (usuario.getRol() != Rol.AUDITOR) {
            bpm.iniciarSesion(UsuarioBpm.de(usuario), req.password());
        }
        JwtService.TokenEmitido token = jwt.emitir(usuario);
        return new LoginResponse(token.token(), token.expiraEn(), UsuarioDto.de(usuario));
    }

    public void logout(Long usuarioId) {
        Usuario usuario = usuarioService.obtener(usuarioId);
        if (usuario.getRol() != Rol.AUDITOR) {
            bpm.cerrarSesion(UsuarioBpm.de(usuario));
        }
    }

    public UsuarioDto me(Long usuarioId) {
        return UsuarioDto.de(usuarioService.obtener(usuarioId));
    }
}
