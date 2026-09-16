package ar.edu.unlp.dssd.rescuesync.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.unlp.dssd.rescuesync.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "rescuesync.debug.registro-habilitado=false")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class RegistroDeshabilitadoIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void sinLaPropiedadLosEndpointsNoExisten() throws Exception {
        mvc.perform(get("/api/auth/registro/organizaciones")).andExpect(status().isNotFound());
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
    }
}
