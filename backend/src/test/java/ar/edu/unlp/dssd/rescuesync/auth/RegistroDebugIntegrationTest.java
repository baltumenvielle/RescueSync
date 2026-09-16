package ar.edu.unlp.dssd.rescuesync.auth;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.unlp.dssd.rescuesync.IntegracionBase;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class RegistroDebugIntegrationTest extends IntegracionBase {

    @Test
    void listaOrganizacionesSinAutenticacion() throws Exception {
        mvc.perform(get("/api/auth/registro/organizaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombre").value(hasItem("Cáritas La Plata")));
    }

    @Test
    void registraUnaOngNuevaEIniciaSesion() throws Exception {
        String token = leer(registrar(datos("ong.debug1", "REPRESENTANTE_ONG", null, "ONG de prueba 1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.rol").value("REPRESENTANTE_ONG"))
                .andExpect(jsonPath("$.usuario.organizacion.tipo").value("ONG"))
                .andExpect(jsonPath("$.usuario.organizacion.nombre").value("ONG de prueba 1")), "$.token");

        mvc.perform(get("/api/ong/convocatorias").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ong.debug1\",\"password\":\"secreta123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void registraUnOperadorEnUnMunicipioExistente() throws Exception {
        registrar(datos("municipio.debug", "OPERADOR_MUNICIPAL", 2L, null))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.organizacion.nombre").value("Municipalidad de Berisso"));
    }

    @Test
    void rechazaUsuarioRepetido() throws Exception {
        registrar(datos("ccr", "CCR", 3L, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.username").value("El nombre de usuario ya está en uso"));
    }

    @Test
    void rechazaOrganizacionDeOtroTipo() throws Exception {
        registrar(datos("ccr.debug", "CCR", 4L, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.organizacionId").exists());
    }

    @Test
    void validaElFormulario() throws Exception {
        Map<String, Object> invalido = datos("A B", "AUDITOR", 7L, "Y otra");
        invalido.put("password", "corta");
        invalido.put("email", "no-es-email");
        registrar(invalido)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.username").exists())
                .andExpect(jsonPath("$.errores.password").exists())
                .andExpect(jsonPath("$.errores.email").exists())
                .andExpect(jsonPath("$.errores.organizacionIndicada").exists());
    }

    private ResultActions registrar(Map<String, Object> body) throws Exception {
        return mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)));
    }

    private static Map<String, Object> datos(String username, String rol, Long organizacionId, String nueva) {
        Map<String, Object> m = new HashMap<>();
        m.put("nombre", "Persona de prueba");
        m.put("email", username.toLowerCase() + "@prueba.test");
        m.put("username", username);
        m.put("password", "secreta123");
        m.put("rol", rol);
        m.put("organizacionId", organizacionId);
        m.put("nuevaOrganizacion", nueva);
        return m;
    }
}
