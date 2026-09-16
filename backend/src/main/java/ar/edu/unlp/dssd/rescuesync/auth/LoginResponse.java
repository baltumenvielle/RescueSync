package ar.edu.unlp.dssd.rescuesync.auth;

import java.time.Instant;

public record LoginResponse(String token, Instant expiraEn, UsuarioDto usuario) {
}
