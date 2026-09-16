package ar.edu.unlp.dssd.rescuesync.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("rescuesync")
public record RescueSyncProperties(Jwt jwt, Internal internal, Bpm bpm) {

    public record Jwt(@NotBlank @Size(min = 32) String secret, @NotNull Duration expiracion) {
    }

    public record Internal(@NotBlank String token) {
    }

    public record Bpm(Tareas tareas, boolean completarRegistroAlInstanciar) {
    }

    /** Nombres de las tareas humanas del modelo de Bonita que la app necesita reconocer. */
    public record Tareas(@NotBlank String registro, @NotBlank String revision, @NotBlank String desglose,
                         @NotBlank String evaluarCobertura) {
    }
}
