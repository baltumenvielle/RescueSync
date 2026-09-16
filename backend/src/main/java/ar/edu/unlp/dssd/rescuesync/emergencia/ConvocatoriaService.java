package ar.edu.unlp.dssd.rescuesync.emergencia;

import ar.edu.unlp.dssd.rescuesync.auditoria.AuditoriaService;
import ar.edu.unlp.dssd.rescuesync.bpm.AccionTarea;
import ar.edu.unlp.dssd.rescuesync.bpm.BpmPort;
import ar.edu.unlp.dssd.rescuesync.bpm.TareaPendiente;
import ar.edu.unlp.dssd.rescuesync.bpm.TareaService;
import ar.edu.unlp.dssd.rescuesync.bpm.UsuarioBpm;
import ar.edu.unlp.dssd.rescuesync.bpm.VariablesProceso;
import ar.edu.unlp.dssd.rescuesync.cobertura.CoberturaService;
import ar.edu.unlp.dssd.rescuesync.cobertura.ResultadoCobertura;
import ar.edu.unlp.dssd.rescuesync.common.NoEncontradoException;
import ar.edu.unlp.dssd.rescuesync.common.ReglaNegocioException;
import ar.edu.unlp.dssd.rescuesync.emergencia.dto.EmergenciaDetalleDto;
import ar.edu.unlp.dssd.rescuesync.emergencia.dto.PublicacionDto;
import ar.edu.unlp.dssd.rescuesync.lote.LoteRepository;
import ar.edu.unlp.dssd.rescuesync.seguridad.UsuarioActual;
import ar.edu.unlp.dssd.rescuesync.usuario.Usuario;
import ar.edu.unlp.dssd.rescuesync.usuario.UsuarioService;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ciclo de la convocatoria: definición por el CCR, publicación y cierre (disparados por el BPM)
 * y decisión ante cobertura insuficiente.
 */
@Service
public class ConvocatoriaService {

    private final EmergenciaRepository emergencias;
    private final LoteRepository lotes;
    private final UsuarioService usuarios;
    private final BpmPort bpm;
    private final TareaService tareas;
    private final CoberturaService cobertura;
    private final AuditoriaService auditoria;
    private final EmergenciaService emergenciaService;
    private final Clock clock;

    public ConvocatoriaService(EmergenciaRepository emergencias, LoteRepository lotes, UsuarioService usuarios,
                               BpmPort bpm, TareaService tareas, CoberturaService cobertura,
                               AuditoriaService auditoria, EmergenciaService emergenciaService, Clock clock) {
        this.emergencias = emergencias;
        this.lotes = lotes;
        this.usuarios = usuarios;
        this.bpm = bpm;
        this.tareas = tareas;
        this.cobertura = cobertura;
        this.auditoria = auditoria;
        this.emergenciaService = emergenciaService;
        this.clock = clock;
    }

    /**
     * Cierre de la tarea humana "Desglose en lotes": fija la duración de la ventana, la escribe en
     * la variable {@code horasVentana} (la usa el timer) y completa la tarea.
     */
    @Transactional
    public EmergenciaDetalleDto confirmarLotes(UsuarioActual actual, Long id, int horasVentana) {
        Usuario ccr = usuarios.obtener(actual.id());
        Emergencia emergencia = bloquear(id);
        UsuarioBpm usuarioBpm = UsuarioBpm.de(ccr);
        TareaPendiente tarea = tareas.exigirTarea(usuarioBpm, emergencia, AccionTarea.DESGLOSE);

        long cantidadLotes = lotes.countByEmergenciaId(id);
        if (cantidadLotes == 0) {
            throw new ReglaNegocioException("Defina al menos un lote de necesidades antes de publicar");
        }
        emergencia.definirConvocatoria(horasVentana);
        bpm.setVariable(usuarioBpm, emergencia.getCaseIdBonita(), VariablesProceso.HORAS_VENTANA, horasVentana);
        tareas.completar(usuarioBpm, tarea);

        auditoria.registrar("EMERGENCIA", id, "LOTES_DEFINIDOS", ccr.getId(),
                Map.of("horasVentana", horasVentana, "cantidadLotes", cantidadLotes));
        return emergenciaService.detalle(actual, id);
    }

    /** Tarea automática "Publicación de convocatoria" (conector REST de Bonita). Idempotente. */
    @Transactional
    public PublicacionDto publicar(Long id) {
        Emergencia emergencia = bloquear(id);
        boolean yaAbierta = emergencia.getEstado() == EstadoEmergencia.CONVOCATORIA_ABIERTA;
        emergencia.abrirConvocatoria(clock.instant());
        if (!yaAbierta) {
            auditoria.registrar("EMERGENCIA", id, "CONVOCATORIA_PUBLICADA", null, Map.of(
                    "horasVentana", emergencia.getHorasVentana(),
                    "fechaCierre", emergencia.getFechaCierreConvocatoria().toString()));
        }
        return new PublicacionDto(id, true, emergencia.getHorasVentana(), emergencia.getFechaAperturaConvocatoria(),
                emergencia.getFechaCierreConvocatoria());
    }

    /**
     * Vencimiento de la ventana (conector de "Evaluación de cobertura"): cierra la convocatoria
     * si seguía abierta y calcula la cobertura. Idempotente.
     */
    @Transactional
    public ResultadoCobertura cerrarYEvaluar(Long id) {
        Emergencia emergencia = bloquear(id);
        boolean estabaAbierta = emergencia.getEstado() == EstadoEmergencia.CONVOCATORIA_ABIERTA;
        emergencia.cerrarConvocatoria();
        ResultadoCobertura resultado = cobertura.calcular(id);
        if (estabaAbierta) {
            auditoria.registrar("EMERGENCIA", id, "CONVOCATORIA_CERRADA", null, Map.of(
                    "coberturaCompleta", resultado.completa(),
                    "lotesCubiertos", resultado.lotesCubiertos(),
                    "lotesTotales", resultado.lotesTotales()));
        }
        return resultado;
    }

    /** Tarea humana "Evaluar cobertura insuficiente" del CCR. */
    @Transactional
    public EmergenciaDetalleDto decidir(UsuarioActual actual, Long id, DecisionCcr decision, Integer horasVentana) {
        Usuario ccr = usuarios.obtener(actual.id());
        Emergencia emergencia = bloquear(id);
        UsuarioBpm usuarioBpm = UsuarioBpm.de(ccr);
        TareaPendiente tarea = tareas.exigirTarea(usuarioBpm, emergencia, AccionTarea.EVALUAR_COBERTURA);

        emergencia.registrarDecision(decision, horasVentana);
        String caseId = emergencia.getCaseIdBonita();
        if (decision == DecisionCcr.REABRIR) {
            bpm.setVariable(usuarioBpm, caseId, VariablesProceso.HORAS_VENTANA, emergencia.getHorasVentana());
        }
        bpm.setVariable(usuarioBpm, caseId, VariablesProceso.DECISION_CCR, decision.name());
        tareas.completar(usuarioBpm, tarea);

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("decision", decision);
        datos.put("horasVentana", emergencia.getHorasVentana());
        auditoria.registrar("EMERGENCIA", id, "DECISION_COBERTURA", ccr.getId(), datos);
        return emergenciaService.detalle(actual, id);
    }

    private Emergencia bloquear(Long id) {
        return emergencias.findParaActualizar(id).orElseThrow(() -> new NoEncontradoException("Emergencia", id));
    }
}
