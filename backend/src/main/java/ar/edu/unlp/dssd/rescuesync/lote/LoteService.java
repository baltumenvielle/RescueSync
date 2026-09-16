package ar.edu.unlp.dssd.rescuesync.lote;

import ar.edu.unlp.dssd.rescuesync.auditoria.AuditoriaService;
import ar.edu.unlp.dssd.rescuesync.common.NoEncontradoException;
import ar.edu.unlp.dssd.rescuesync.common.ReglaNegocioException;
import ar.edu.unlp.dssd.rescuesync.emergencia.AccesoEmergencias;
import ar.edu.unlp.dssd.rescuesync.emergencia.Emergencia;
import ar.edu.unlp.dssd.rescuesync.emergencia.EmergenciaRepository;
import ar.edu.unlp.dssd.rescuesync.lote.dto.LoteDto;
import ar.edu.unlp.dssd.rescuesync.lote.dto.LoteRequest;
import ar.edu.unlp.dssd.rescuesync.seguridad.UsuarioActual;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** ABM de lotes de necesidades (tarea "Desglose en lotes" del CCR). */
@Service
public class LoteService {

    private final LoteRepository lotes;
    private final EmergenciaRepository emergencias;
    private final AccesoEmergencias acceso;
    private final AuditoriaService auditoria;

    public LoteService(LoteRepository lotes, EmergenciaRepository emergencias, AccesoEmergencias acceso,
                       AuditoriaService auditoria) {
        this.lotes = lotes;
        this.emergencias = emergencias;
        this.acceso = acceso;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<LoteDto> listar(UsuarioActual actual, Long emergenciaId) {
        acceso.visible(emergenciaId, actual);
        return lotes.findByEmergenciaIdOrderById(emergenciaId).stream().map(LoteDto::de).toList();
    }

    @Transactional
    public LoteDto crear(UsuarioActual actual, Long emergenciaId, LoteRequest req) {
        Emergencia emergencia = editable(emergenciaId);
        Lote lote = lotes.save(new Lote(emergencia, req.tipo(), req.descripcion().trim(), req.unidad().trim(),
                req.cantidadRequerida(), req.prioridad()));
        auditoria.registrar("LOTE", lote.getId(), "CREADO", actual.id(), datos(emergenciaId, lote));
        return LoteDto.de(lote);
    }

    @Transactional
    public LoteDto actualizar(UsuarioActual actual, Long emergenciaId, Long loteId, LoteRequest req) {
        editable(emergenciaId);
        Lote lote = lote(emergenciaId, loteId);
        lote.actualizar(req.tipo(), req.descripcion().trim(), req.unidad().trim(), req.cantidadRequerida(),
                req.prioridad());
        auditoria.registrar("LOTE", loteId, "ACTUALIZADO", actual.id(), datos(emergenciaId, lote));
        return LoteDto.de(lote);
    }

    @Transactional
    public void eliminar(UsuarioActual actual, Long emergenciaId, Long loteId) {
        editable(emergenciaId);
        Lote lote = lote(emergenciaId, loteId);
        if (lotes.estaReferenciadoEnOfertas(loteId)) {
            throw new ReglaNegocioException("El lote tiene ofertas registradas y no puede eliminarse; "
                    + "modifique su cantidad o descripción");
        }
        lotes.delete(lote);
        auditoria.registrar("LOTE", loteId, "ELIMINADO", actual.id(), datos(emergenciaId, lote));
    }

    private Emergencia editable(Long emergenciaId) {
        Emergencia emergencia = emergencias.findParaActualizar(emergenciaId)
                .orElseThrow(() -> new NoEncontradoException("Emergencia", emergenciaId));
        emergencia.exigirEdicionDeLotes();
        return emergencia;
    }

    private Lote lote(Long emergenciaId, Long loteId) {
        return lotes.findByIdAndEmergenciaId(loteId, emergenciaId)
                .orElseThrow(() -> new NoEncontradoException("Lote", loteId));
    }

    private static Map<String, Object> datos(Long emergenciaId, Lote lote) {
        return Map.of("emergenciaId", emergenciaId, "tipo", lote.getTipo(), "descripcion", lote.getDescripcion(),
                "unidad", lote.getUnidad(), "cantidadRequerida", lote.getCantidadRequerida(),
                "prioridad", lote.getPrioridad());
    }
}
