package ar.edu.unlp.dssd.rescuesync.auth;

import ar.edu.unlp.dssd.rescuesync.seguridad.SesionActual;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final SesionActual sesion;

    public AuthController(AuthService auth, SesionActual sesion) {
        this.auth = auth;
        this.sesion = sesion;
    }

    @PostMapping("/login")
    LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    /** El JWT es stateless: la SPA lo descarta. Acá se cierra la sesión contra el motor. */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout() {
        auth.logout(sesion.usuario().id());
    }

    @GetMapping("/me")
    UsuarioDto me() {
        return auth.me(sesion.usuario().id());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    ProblemDetail credencialesInvalidas(CredencialesInvalidasException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
        problema.setTitle("Credenciales inválidas");
        return problema;
    }
}
