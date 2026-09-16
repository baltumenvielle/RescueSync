package ar.edu.unlp.dssd.rescuesync.flujo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.unlp.dssd.rescuesync.IntegracionBase;
import ar.edu.unlp.dssd.rescuesync.bpm.TareaPendiente;
import ar.edu.unlp.dssd.rescuesync.bpm.VariablesProceso;
import ar.edu.unlp.dssd.rescuesync.bpm.fake.FakeBpmAdapter.LlamadaBpm;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Circuito de la Entrega 2 contra PostgreSQL real y {@code FakeBpmAdapter}: alta de emergencia,
 * revisión y desglose del CCR, publicación, ofertas versionadas, cierre por timer y decisión
 * ante cobertura insuficiente.
 */
class FlujoConvocatoriaIntegrationTest extends IntegracionBase {

    private static final String MUNICIPIO = "municipio.laplata";
    private static final String OTRO_MUNICIPIO = "municipio.berisso";
    private static final String CCR = "ccr";
    private static final String CRUZ_ROJA = "ong.cruzroja";
    private static final String CARITAS = "ong.caritas";
    private static final String AUDITOR = "auditor";

    @Test
    void circuitoCompletoConCoberturaInsuficienteYReapertura() throws Exception {
        // 1. Municipio registra la emergencia -> se instancia el caso y se completa "Registro"
        long emergenciaId = registrarEmergencia();
        String caseId = leer(getComo(MUNICIPIO, "/api/emergencias/{id}", emergenciaId), "$.caseId");
        assertThat(caseId).isNotBlank();
        assertThat(llamadas(caseId, "iniciarCaso")).hasSize(1);
        assertThat(bpm.variables(caseId)).containsEntry(VariablesProceso.EMERGENCIA_ID, emergenciaId);
        assertThat(nombresTareas(caseId)).containsExactly("Revisión de Información");

        // 2. CCR ve la tarea en su bandeja y revisa
        getComo(CCR, "/api/tareas")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.caseId == '%s')].accion".formatted(caseId)).value(hasItem("REVISION")));
        putComo(CCR, Map.of("observaciones", "Información verificada con Defensa Civil"),
                "/api/emergencias/{id}/revision", emergenciaId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_REVISION"))
                .andExpect(jsonPath("$.revisadaPor").value("Carla Coordinadora"));
        assertThat(nombresTareas(caseId)).containsExactly("Desglose en lotes de necesidades");

        // 3. Desglose en lotes
        long paramedicos = id(postComo(CCR, lote("PERSONAL", "Paramédicos", "personas", 5),
                "/api/emergencias/{id}/lotes", emergenciaId).andExpect(status().isCreated()));
        long raciones = id(postComo(CCR, lote("RECURSO", "Raciones de alimento", "raciones", 1000),
                "/api/emergencias/{id}/lotes", emergenciaId).andExpect(status().isCreated()));
        long descartable = id(postComo(CCR, lote("RECURSO", "Frazadas", "unidades", 50),
                "/api/emergencias/{id}/lotes", emergenciaId));
        como(CCR, delete("/api/emergencias/{id}/lotes/{loteId}", emergenciaId, descartable))
                .andExpect(status().isNoContent());

        postComo(CCR, Map.of("horasVentana", 48), "/api/emergencias/{id}/convocatoria", emergenciaId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("LOTES_DEFINIDOS"))
                .andExpect(jsonPath("$.lotes", hasSize(2)));
        assertThat(bpm.variables(caseId)).containsEntry(VariablesProceso.HORAS_VENTANA, 48);
        assertThat(nombresTareas(caseId)).isEmpty();

        // Con los lotes definidos ya no se pueden editar
        postComo(CCR, lote("RECURSO", "Agua", "litros", 10), "/api/emergencias/{id}/lotes", emergenciaId)
                .andExpect(status().isConflict());
        // Todavía no es visible para las ONGs
        getComo(CRUZ_ROJA, "/api/emergencias/{id}", emergenciaId).andExpect(status().isForbidden());

        // 4. Conector de Bonita: publicación (idempotente)
        interno(post("/internal/bpm/emergencias/{id}/convocatoria/publicar", emergenciaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicada").value(true))
                .andExpect(jsonPath("$.fechaCierre").value("2026-10-03T12:00:00Z"));
        reloj.avanzar(Duration.ofMinutes(5));
        interno(post("/internal/bpm/emergencias/{id}/convocatoria/publicar", emergenciaId))
                .andExpect(jsonPath("$.fechaCierre").value("2026-10-03T12:00:00Z"));

        // 5. ONGs cargan ofertas parciales
        getComo(CRUZ_ROJA, "/api/ong/convocatorias")
                .andExpect(jsonPath("$[?(@.emergencia.id == %d)].lotes.length()".formatted(emergenciaId))
                        .value(hasItem(2)));
        long ofertaCruzRoja = id(postComo(CRUZ_ROJA, oferta("Equipo inicial", Map.of(paramedicos, 3)),
                "/api/emergencias/{id}/ofertas", emergenciaId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.versionActual").value(1)));
        postComo(CRUZ_ROJA, oferta(null, Map.of(paramedicos, 1)), "/api/emergencias/{id}/ofertas", emergenciaId)
                .andExpect(status().isConflict());

        // Edición: nueva versión, la anterior queda intacta
        putComo(CRUZ_ROJA, oferta("Sumamos raciones", Map.of(paramedicos, 5, raciones, 400)),
                "/api/ofertas/{id}", ofertaCruzRoja)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versionActual").value(2))
                .andExpect(jsonPath("$.vigente.items", hasSize(2)));
        getComo(CRUZ_ROJA, "/api/ofertas/{id}/versiones", ofertaCruzRoja)
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].numero").value(1))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].cantidadOfrecida").value(3))
                .andExpect(jsonPath("$[1].comentario").value("Sumamos raciones"));

        // Lote de otra emergencia o ajeno -> rechazado
        putComo(CRUZ_ROJA, oferta(null, Map.of(999_999L, 1)), "/api/ofertas/{id}", ofertaCruzRoja)
                .andExpect(status().isConflict());
        // Otra ONG no puede ver ni editar la oferta
        getComo(CARITAS, "/api/ofertas/{id}", ofertaCruzRoja).andExpect(status().isForbidden());
        putComo(CARITAS, oferta(null, Map.of(raciones, 1)), "/api/ofertas/{id}", ofertaCruzRoja)
                .andExpect(status().isForbidden());

        postComo(CRUZ_ROJA, null, "/api/ofertas/{id}/enviar", ofertaCruzRoja)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENVIADA"));

        long ofertaCaritas = id(postComo(CARITAS, oferta(null, Map.of(raciones, 300)),
                "/api/emergencias/{id}/ofertas", emergenciaId));
        postComo(CARITAS, null, "/api/ofertas/{id}/enviar", ofertaCaritas).andExpect(status().isOk());

        getComo(CCR, "/api/emergencias/{id}/cobertura", emergenciaId)
                .andExpect(jsonPath("$.completa").value(false))
                .andExpect(jsonPath("$.lotes[0].cantidadOfrecida").value(5))
                .andExpect(jsonPath("$.lotes[1].cantidadOfrecida").value(700));

        // 6. Vence la ventana: la app rechaza ofertas aunque el conector todavía no haya cerrado
        reloj.avanzar(Duration.ofHours(48));
        putComo(CARITAS, oferta(null, Map.of(raciones, 700)), "/api/ofertas/{id}", ofertaCaritas)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("La ventana de recepción de ofertas ya venció"));

        // Conector de evaluación de cobertura
        interno(get("/internal/bpm/emergencias/{id}/cobertura", emergenciaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completa").value(false))
                .andExpect(jsonPath("$.lotesCubiertos").value(1))
                .andExpect(jsonPath("$.lotesTotales").value(2));
        interno(get("/internal/bpm/emergencias/{id}/ofertas", emergenciaId))
                .andExpect(jsonPath("$.cantidad").value(2))
                .andExpect(jsonPath("$.ofertas[0].version").value(2))
                .andExpect(jsonPath("$.ofertas[0].ongCuit").value("30-99900004-4"));
        getComo(MUNICIPIO, "/api/emergencias/{id}", emergenciaId)
                .andExpect(jsonPath("$.estado").value("CONVOCATORIA_CERRADA"));

        // 7. Gateway: cobertura incompleta -> vuelve al CCR, que decide reabrir con nueva ventana
        bpm.simularFinVentana(caseId, false);
        postComo(CCR, Map.of("decision", "REABRIR", "horasVentana", 24),
                "/api/emergencias/{id}/decision-cobertura", emergenciaId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("LOTES_DEFINIDOS"))
                .andExpect(jsonPath("$.decisionCcr").value("REABRIR"));
        assertThat(bpm.variables(caseId))
                .containsEntry(VariablesProceso.DECISION_CCR, "REABRIR")
                .containsEntry(VariablesProceso.HORAS_VENTANA, 24);

        interno(post("/internal/bpm/emergencias/{id}/convocatoria/publicar", emergenciaId))
                .andExpect(jsonPath("$.horasVentana").value(24));
        putComo(CARITAS, oferta("Completamos raciones", Map.of(raciones, 700)), "/api/ofertas/{id}", ofertaCaritas)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versionActual").value(2));
        getComo(CCR, "/api/emergencias/{id}/cobertura", emergenciaId)
                .andExpect(jsonPath("$.completa").value(true));

        // 8. Trazabilidad para el auditor
        getComo(AUDITOR, "/api/auditoria/eventos?entidad=OFERTA&entidadId={id}", ofertaCruzRoja)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3));
        getComo(AUDITOR, "/api/emergencias/{id}/ofertas", emergenciaId)
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void reformularVuelveAlDesgloseYNoPermiteBorrarLotesConOfertas() throws Exception {
        long emergenciaId = registrarEmergencia();
        String caseId = leer(getComo(CCR, "/api/emergencias/{id}", emergenciaId), "$.caseId");
        putComo(CCR, Map.of(), "/api/emergencias/{id}/revision", emergenciaId).andExpect(status().isOk());
        long loteId = id(postComo(CCR, lote("RECURSO", "Botes", "unidades", 4),
                "/api/emergencias/{id}/lotes", emergenciaId));
        postComo(CCR, Map.of("horasVentana", 6), "/api/emergencias/{id}/convocatoria", emergenciaId);
        interno(post("/internal/bpm/emergencias/{id}/convocatoria/publicar", emergenciaId));
        postComo("ong.bomberos", oferta(null, Map.of(loteId, 1)), "/api/emergencias/{id}/ofertas", emergenciaId)
                .andExpect(status().isCreated());

        reloj.avanzar(Duration.ofHours(7));
        interno(get("/internal/bpm/emergencias/{id}/cobertura", emergenciaId)).andExpect(jsonPath("$.completa").value(false));
        bpm.simularFinVentana(caseId, false);

        postComo(CCR, Map.of("decision", "REFORMULAR"), "/api/emergencias/{id}/decision-cobertura", emergenciaId)
                .andExpect(jsonPath("$.estado").value("EN_REVISION"))
                .andExpect(jsonPath("$.admiteEdicionDeLotes").value(true));
        assertThat(nombresTareas(caseId)).containsExactly("Desglose en lotes de necesidades");

        como(CCR, delete("/api/emergencias/{id}/lotes/{loteId}", emergenciaId, loteId))
                .andExpect(status().isConflict());
        putComo(CCR, lote("RECURSO", "Botes", "unidades", 2), "/api/emergencias/{id}/lotes/{loteId}", emergenciaId, loteId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadRequerida").value(2));
    }

    @Test
    void continuarParcialPasaAValidacion() throws Exception {
        long emergenciaId = registrarEmergencia();
        String caseId = leer(getComo(CCR, "/api/emergencias/{id}", emergenciaId), "$.caseId");
        putComo(CCR, Map.of(), "/api/emergencias/{id}/revision", emergenciaId);
        postComo(CCR, lote("PERSONAL", "Rescatistas", "personas", 10), "/api/emergencias/{id}/lotes", emergenciaId);
        postComo(CCR, Map.of("horasVentana", 1), "/api/emergencias/{id}/convocatoria", emergenciaId);
        como(CCR, post("/api/dev/bpm/emergencias/{id}/publicar", emergenciaId)).andExpect(status().isOk());
        reloj.avanzar(Duration.ofHours(2));

        como(CCR, post("/api/dev/bpm/emergencias/{id}/fin-ventana", emergenciaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completa").value(false));
        assertThat(nombresTareas(caseId)).containsExactly("Evaluar cobertura insuficiente");

        postComo(CCR, Map.of("decision", "CONTINUAR_PARCIAL"), "/api/emergencias/{id}/decision-cobertura", emergenciaId)
                .andExpect(jsonPath("$.estado").value("EN_VALIDACION"));
        assertThat(bpm.variables(caseId)).containsEntry(VariablesProceso.DECISION_CCR, "CONTINUAR_PARCIAL");
        assertThat(nombresTareas(caseId)).isEmpty();
    }

    @Test
    void lasTareasSoloSePuedenCompletarCuandoElProcesoLasHabilita() throws Exception {
        long emergenciaId = registrarEmergencia();

        // Sin revisión previa, no existe la tarea de desglose
        postComo(CCR, Map.of("horasVentana", 10), "/api/emergencias/{id}/convocatoria", emergenciaId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("no está disponible")));
        // No hay tarea de decisión pendiente
        postComo(CCR, Map.of("decision", "REABRIR"), "/api/emergencias/{id}/decision-cobertura", emergenciaId)
                .andExpect(status().isConflict());
        // Sin lotes no se puede cerrar el desglose
        putComo(CCR, Map.of(), "/api/emergencias/{id}/revision", emergenciaId);
        postComo(CCR, Map.of("horasVentana", 10), "/api/emergencias/{id}/convocatoria", emergenciaId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Defina al menos un lote de necesidades antes de publicar"));
    }

    @Test
    void validaElFormularioDeAltaDeEmergencia() throws Exception {
        postComo(MUNICIPIO, Map.of("tipoDesastre", "INUNDACION", "zonaAfectada", " ", "descripcion", "corta"),
                "/api/emergencias")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.gravedad").exists())
                .andExpect(jsonPath("$.errores.zonaAfectada").exists())
                .andExpect(jsonPath("$.errores.descripcion").exists());
    }

    @Test
    void rbacYAlcancePorOrganizacion() throws Exception {
        long emergenciaId = registrarEmergencia();

        // Solo el operador municipal registra emergencias
        postComo(CCR, emergenciaValida(), "/api/emergencias").andExpect(status().isForbidden());
        postComo(CRUZ_ROJA, emergenciaValida(), "/api/emergencias").andExpect(status().isForbidden());
        // Un municipio no ve emergencias de otro
        getComo(OTRO_MUNICIPIO, "/api/emergencias/{id}", emergenciaId).andExpect(status().isForbidden());
        List<Integer> idsOtro = leer(getComo(OTRO_MUNICIPIO, "/api/emergencias"), "$[*].id");
        assertThat(idsOtro).doesNotContain((int) emergenciaId);
        // Solo el CCR gestiona lotes y revisión
        putComo(MUNICIPIO, Map.of(), "/api/emergencias/{id}/revision", emergenciaId).andExpect(status().isForbidden());
        postComo(MUNICIPIO, lote("RECURSO", "x", "u", 1), "/api/emergencias/{id}/lotes", emergenciaId)
                .andExpect(status().isForbidden());
        // El auditor lee todo pero no opera ni tiene bandeja
        getComo(AUDITOR, "/api/emergencias/{id}", emergenciaId).andExpect(status().isOk());
        putComo(AUDITOR, Map.of(), "/api/emergencias/{id}/revision", emergenciaId).andExpect(status().isForbidden());
        getComo(AUDITOR, "/api/tareas").andExpect(status().isForbidden());
        getComo(CCR, "/api/auditoria/eventos").andExpect(status().isForbidden());
        // Sin token
        mvc.perform(get("/api/emergencias")).andExpect(status().isUnauthorized());
    }

    @Test
    void endpointsInternosExigenElTokenCompartido() throws Exception {
        long emergenciaId = registrarEmergencia();

        mvc.perform(get("/internal/bpm/emergencias/{id}/cobertura", emergenciaId))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/internal/bpm/emergencias/{id}/cobertura", emergenciaId).header("X-Internal-Token", "otro"))
                .andExpect(status().isUnauthorized());
        // Un JWT válido de la app no sirve para los endpoints internos
        getComo(CCR, "/internal/bpm/emergencias/{id}/cobertura", emergenciaId).andExpect(status().isUnauthorized());
        // Publicar antes de definir lotes es un conflicto, no un error del servidor
        interno(post("/internal/bpm/emergencias/{id}/convocatoria/publicar", emergenciaId))
                .andExpect(status().isConflict());
        interno(post("/internal/bpm/emergencias/{id}/convocatoria/publicar", 987654))
                .andExpect(status().isNotFound());
    }

    @Test
    void loginInvalidoDevuelve401YElValidoDevuelveElPerfil() throws Exception {
        mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content("{\"username\":\"ccr\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
        getComo(CRUZ_ROJA, "/api/auth/me")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("REPRESENTANTE_ONG"))
                .andExpect(jsonPath("$.organizacion.nombre").value("Cruz Roja Argentina - Filial La Plata"));
    }

    private long registrarEmergencia() throws Exception {
        ResultActions r = postComo(MUNICIPIO, emergenciaValida(), "/api/emergencias")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("REGISTRADA"))
                .andExpect(jsonPath("$.municipio.nombre").value("Municipalidad de La Plata"));
        return id(r);
    }

    private static Map<String, Object> emergenciaValida() {
        return Map.of("tipoDesastre", "INUNDACION", "gravedad", "ALTA", "zonaAfectada", "Barrio Tolosa",
                "descripcion", "Desborde del arroyo del Gato con 200 familias evacuadas");
    }

    private static Map<String, Object> lote(String tipo, String descripcion, String unidad, int cantidad) {
        return Map.of("tipo", tipo, "descripcion", descripcion, "unidad", unidad, "cantidadRequerida", cantidad,
                "prioridad", "ALTA");
    }

    private static Map<String, Object> oferta(String comentario, Map<Long, Integer> cantidades) {
        List<Map<String, Object>> items = cantidades.entrySet().stream()
                .map(e -> Map.<String, Object>of("loteId", e.getKey(), "cantidad", e.getValue()))
                .toList();
        return comentario == null ? Map.of("items", items) : Map.of("comentario", comentario, "items", items);
    }

    private List<String> nombresTareas(String caseId) {
        return bpm.tareasDelCaso(caseId).stream().map(TareaPendiente::nombreTarea).toList();
    }

    private List<LlamadaBpm> llamadas(String caseId, String operacion) {
        return bpm.llamadas(operacion).stream().filter(l -> caseId.equals(l.argumentos().get("caseId"))).toList();
    }
}
