package ar.edu.unlp.dssd.rescuesync.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Autentica las llamadas de los conectores REST de Bonita a /internal/bpm/** mediante
 * el header compartido X-Internal-Token.
 */
public class InternalTokenFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Internal-Token";
    public static final String ROL_INTERNO = "ROLE_BPM_INTERNO";

    private final byte[] tokenEsperado;

    public InternalTokenFilter(String tokenEsperado) {
        this.tokenEsperado = tokenEsperado.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String recibido = request.getHeader(HEADER);
        if (recibido == null || !MessageDigest.isEqual(tokenEsperado, recibido.getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(
                    "{\"status\":401,\"title\":\"No autorizado\",\"detail\":\"Token interno ausente o inválido\"}");
            return;
        }
        var auth = new UsernamePasswordAuthenticationToken("bonita", null,
                List.of(new SimpleGrantedAuthority(ROL_INTERNO)));
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
