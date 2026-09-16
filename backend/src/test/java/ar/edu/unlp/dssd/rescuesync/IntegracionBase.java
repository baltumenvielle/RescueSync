package ar.edu.unlp.dssd.rescuesync;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.unlp.dssd.rescuesync.bpm.fake.FakeBpmAdapter;
import com.jayway.jsonpath.JsonPath;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, IntegracionBase.RelojConfig.class})
public abstract class IntegracionBase {

    protected static final String TOKEN_INTERNO = "dev-internal-token";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected JsonMapper json;

    @Autowired
    protected FakeBpmAdapter bpm;

    @Autowired
    protected RelojTest reloj;

    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    @BeforeEach
    void reiniciarReloj() {
        reloj.fijar(Fixtures.T0);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class RelojConfig {
        @Bean
        @Primary
        RelojTest relojTest() {
            return new RelojTest(Fixtures.T0);
        }
    }

    protected String token(String username) {
        return tokens.computeIfAbsent(username, u -> {
            try {
                String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                                .content(json.writeValueAsString(Map.of("username", u, "password", "rescuesync"))))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();
                return JsonPath.read(body, "$.token");
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    protected ResultActions como(String username, MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + token(username)));
    }

    protected ResultActions getComo(String username, String url, Object... vars) throws Exception {
        return como(username, get(url, vars));
    }

    protected ResultActions postComo(String username, Object body, String url, Object... vars) throws Exception {
        return como(username, post(url, vars).contentType(MediaType.APPLICATION_JSON).content(cuerpo(body)));
    }

    protected ResultActions putComo(String username, Object body, String url, Object... vars) throws Exception {
        return como(username, put(url, vars).contentType(MediaType.APPLICATION_JSON).content(cuerpo(body)));
    }

    protected ResultActions interno(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header("X-Internal-Token", TOKEN_INTERNO));
    }

    protected static <T> T leer(ResultActions resultado, String path) throws Exception {
        return JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), path);
    }

    protected static long id(ResultActions resultado) throws Exception {
        return ((Number) leer(resultado, "$.id")).longValue();
    }

    private String cuerpo(Object body) {
        return body == null ? "{}" : json.writeValueAsString(body);
    }
}
