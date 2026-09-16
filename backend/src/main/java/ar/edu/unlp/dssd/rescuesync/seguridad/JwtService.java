package ar.edu.unlp.dssd.rescuesync.seguridad;

import ar.edu.unlp.dssd.rescuesync.config.RescueSyncProperties;
import ar.edu.unlp.dssd.rescuesync.usuario.Rol;
import ar.edu.unlp.dssd.rescuesync.usuario.Usuario;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

/** Emisión y verificación de JWT firmados con HMAC-SHA256. */
@Service
public class JwtService {

    static final String ISSUER = "rescuesync";
    static final String CLAIM_UID = "uid";
    static final String CLAIM_ROL = "rol";
    static final String CLAIM_ORG = "org";

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final RescueSyncProperties properties;
    private final Clock clock;

    public JwtService(RescueSyncProperties properties, Clock clock) {
        SecretKey key = new SecretKeySpec(properties.jwt().secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.encoder = NimbusJwtEncoder.withSecretKey(key).build();
        this.decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        this.properties = properties;
        this.clock = clock;
    }

    public TokenEmitido emitir(Usuario usuario) {
        Instant ahora = clock.instant();
        Instant expira = ahora.plus(properties.jwt().expiracion());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(usuario.getUsername())
                .issuedAt(ahora)
                .expiresAt(expira)
                .claim(CLAIM_UID, usuario.getId())
                .claim(CLAIM_ROL, usuario.getRol().name())
                .claim(CLAIM_ORG, usuario.getOrganizacion().getId())
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new TokenEmitido(token, expira);
    }

    public JwtDecoder decoder() {
        return decoder;
    }

    public static UsuarioActual usuarioDe(Jwt jwt) {
        return new UsuarioActual(
                ((Number) jwt.getClaim(CLAIM_UID)).longValue(),
                jwt.getSubject(),
                Rol.valueOf(jwt.getClaimAsString(CLAIM_ROL)),
                ((Number) jwt.getClaim(CLAIM_ORG)).longValue());
    }

    public record TokenEmitido(String token, Instant expiraEn) {
    }
}
