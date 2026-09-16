package ar.edu.unlp.dssd.rescuesync.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TiempoConfig {

    /** Reloj inyectable para poder testear las reglas de ventana temporal. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
