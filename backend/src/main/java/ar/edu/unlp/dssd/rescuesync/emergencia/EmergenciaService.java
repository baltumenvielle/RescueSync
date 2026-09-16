package ar.edu.unlp.dssd.rescuesync.emergencia;

import ar.edu.unlp.dssd.rescuesync.auditoria.AuditoriaService;
import ar.edu.unlp.dssd.rescuesync.bpm.AccionTarea;
import ar.edu.unlp.dssd.rescuesync.bpm.BpmPort;
import ar.edu.unlp.dssd.rescuesync.bpm.TareaPendiente;
import ar.edu.unlp.dssd.rescuesync.bpm.TareaService;
import ar.edu.unlp.dssd.rescuesync.bpm.UsuarioBpm;
import ar.edu.unlp.dssd.rescuesync.common.NoEncontradoException;
import ar.edu.unlp.dssd.rescuesync.config.RescueSyncProperties;
import ar.edu.unlp.dssd.rescuesync.emergencia.dto.CrearEmergenciaRequest;
import ar.edu.unlp.dssd.rescuesync.emergencia.dto.EmergenciaDetalleDto;
import ar.edu.unlp.dssd.rescuesync.emergencia.dto.EmergenciaResumenDto;
import ar.edu.unlp.dssd.rescuesync.lote.LoteRepository;
import ar.edu.unlp.dssd.rescuesync.lote.dto.LoteDto;
import ar.edu.unlp.dssd.rescuesync.seguridad.UsuarioActual;
import ar.edu.unlp.dssd.rescuesync.usuario.Rol;
import ar.edu.unlp.dssd.rescuesync.usuario.Usuario;
import ar.edu.unlp.dssd.rescuesync.usuario.UsuarioService;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmergenciaService {

    private final EmergenciaRepository emergencias;
    private final LoteRepository lotes;
    private final AccesoEmergencias acceso;
    private final UsuarioService usuarios;
    private final BpmPort bpm;
    private final TareaService tareas;
    private final AuditoriaService auditoria;
    private final RescueSyncProperties properties;
    private final Clock clock;

    public EmergenciaService(EmergenciaRepository emergencias, LoteRepository lotes, AccesoEmergencias acceso,
                             UsuarioService usuarios, BpmPort bpm, TareaService tareas, AuditoriaService auditoria,
                             RescueSyncProperties properties, Clock clock) {
        this.emergencias = emergencias;
        this.lotes = lotes;
        this.acceso = acceso;
        this.usuarios = usuarios;
        this.bpm = bpm;
        this.tareas = tareas;
        this.auditoria = auditoria;
        this.properties = properties;
        this.clock = clock;
    }

    /** Registra la emergencia en la base local e instancia el caso en el motor de procesos. */
    @Transactional
    public EmergenciaDetalleDto registrar(UsuarioActual actual, CrearEmergenciaRequest req) {
        Usuario operador = usuarios.obtener(actual.id());
        Emergencia emergencia = emergencias.saveAndFlush(new Emergencia(operador.getOrganizacion(),
                req.tipoDesastre(), req.gravedad(), req.zonaAfectada().trim(), req.descripcion().trim(), operador,
                clock.instant()));

        UsuarioBpm usuarioBpm = UsuarioBpm.de(operador);
        String caseId = bpm.iniciarCaso(usuarioBpm, emergencia.getId());
        emergencia.asociarCaso(caseId);
        if (properties.bpm().completarRegistroAlInstanciar()) {
            tareas.completarSiDisponible(usuarioBpm, caseId, AccionTarea.REGISTRO);
        }

        auditoria.registrar("EMERGENCIA", emergencia.getId(), "REGISTRADA", operador.getId(), Map.of(
                "tipoDesastre", req.tipoDesastre(), "gravedad", req.gravedad(), "zonaAfectada",
                emergencia.getZonaAfectada(), "caseId", caseId));
        return EmergenciaDetalleDto.de(emergencia, List.of());
    }

    @Transactional(readOnly = true)
    public List<EmergenciaResumenDto> listar(UsuarioActual actual, EstadoEmergencia estado) {
        List<Emergencia> resultado = actual.es(Rol.OPERADOR_MUNICIPAL)
                ? emergencias.findByMunicipioIdOrderByCreatedAtDesc(actual.organizacionId())
                : emergencias.findAllByOrderByCreatedAtDesc();
        return resultado.stream()
                .filter(e -> estado == null || e.getEstado() == estado)
                .map(EmergenciaResumenDto::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmergenciaDetalleDto detalle(UsuarioActual actual, Long id) {
        Emergencia emergencia = acceso.visible(id, actual);
        return EmergenciaDetalleDto.de(emergencia,
                lotes.findByEmergenciaIdOrderById(id).stream().map(LoteDto::de).toList());
    }

    /** Tarea humana "Revisión de información" del CCR. */
    @Transactional
    public EmergenciaDetalleDto revisar(UsuarioActual actual, Long id, String observaciones) {
        Usuario ccr = usuarios.obtener(actual.id());
        Emergencia emergencia = emergencias.findParaActualizar(id)
                .orElseThrow(() -> new NoEncontradoException("Emergencia", id));
        UsuarioBpm usuarioBpm = UsuarioBpm.de(ccr);
        TareaPendiente tarea = tareas.exigirTarea(usuarioBpm, emergencia, AccionTarea.REVISION);

        String texto = observaciones == null || observaciones.isBlank() ? null : observaciones.trim();
        emergencia.revisar(ccr, texto, clock.instant());
        tareas.completar(usuarioBpm, tarea);

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("observaciones", texto);
        auditoria.registrar("EMERGENCIA", id, "REVISADA", ccr.getId(), datos);
        return detalle(actual, id);
    }
}
