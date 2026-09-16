package ar.edu.unlp.dssd.rescuesync.auth.registro;

import ar.edu.unlp.dssd.rescuesync.auth.AuthService;
import ar.edu.unlp.dssd.rescuesync.auth.LoginRequest;
import ar.edu.unlp.dssd.rescuesync.auth.LoginResponse;
import ar.edu.unlp.dssd.rescuesync.organizacion.OrganizacionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registro abierto de cuentas de prueba, para depurar la app con distintos perfiles.
 * Solo existe con {@code rescuesync.debug.registro-habilitado=true}; con la propiedad en
 * {@code false} los endpoints responden 404.
 */
@RestController
@RequestMapping("/api/auth/registro")
@ConditionalOnProperty(name = "rescuesync.debug.registro-habilitado", havingValue = "true")
@SecurityRequirements
public class RegistroDebugController {

    private final RegistroDebugService registro;
    private final AuthService auth;

    public RegistroDebugController(RegistroDebugService registro, AuthService auth) {
        this.registro = registro;
        this.auth = auth;
    }

    @Operation(summary = "Organizaciones disponibles para el registro de prueba")
    @GetMapping("/organizaciones")
    List<OrganizacionDto> organizaciones() {
        return registro.organizaciones();
    }

    @Operation(summary = "Crea una cuenta de prueba e inicia sesión con ella")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    LoginResponse registrar(@Valid @RequestBody RegistroDebugRequest req) {
        registro.registrar(req);
        return auth.login(new LoginRequest(req.username(), req.password()));
    }
}
