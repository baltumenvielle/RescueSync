package ar.edu.unlp.dssd.rescuesync.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI rescueSyncOpenApi() {
        return new OpenAPI()
                .info(new Info().title("RescueSync API").version("0.2")
                        .description("Backend de la aplicación web de RescueSync (DSSD 2026 - Grupo 9)"))
                .components(new Components()
                        .addSecuritySchemes("jwt", new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT"))
                        .addSecuritySchemes("internal", new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER).name("X-Internal-Token")))
                .addSecurityItem(new SecurityRequirement().addList("jwt"));
    }
}
