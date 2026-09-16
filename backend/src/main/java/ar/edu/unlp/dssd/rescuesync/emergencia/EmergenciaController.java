package ar.edu.unlp.dssd.rescuesync.emergencia;

import ar.edu.unlp.dssd.rescuesync.cobertura.CoberturaService;
import ar.edu.unlp.dssd.rescuesync.cobertura.ResultadoCobertura;
import ar.edu.unlp.dssd.rescuesync.emergencia.dto.ConvocatoriaRequest;
import ar.edu.unlp.dssd.rescuesync.emergencia.dto.CrearEmergenciaRequest;
import ar.edu.unlp.dssd.rescuesync.emergencia.dto.DecisionCoberturaRequest;
import ar.edu.unlp.dssd.rescuesync.emergencia.dto.EmergenciaDetalleDto;
import ar.edu.unlp.dssd.rescuesync.emergencia.dto.EmergenciaResumenDto;
import ar.edu.unlp.dssd.rescuesync.emergencia.dto.RevisionRequest;
import ar.edu.unlp.dssd.rescuesync.seguridad.SesionActual;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emergencias")
public class EmergenciaController {

    private final EmergenciaService emergencias;
    private final ConvocatoriaService convocatorias;
    private final CoberturaService cobertura;
    private final AccesoEmergencias acceso;
    private final SesionActual sesion;

    public EmergenciaController(EmergenciaService emergencias, ConvocatoriaService convocatorias,
                                CoberturaService cobertura, AccesoEmergencias acceso, SesionActual sesion) {
        this.emergencias = emergencias;
        this.convocatorias = convocatorias;
        this.cobertura = cobertura;
        this.acceso = acceso;
        this.sesion = sesion;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OPERADOR_MUNICIPAL')")
    EmergenciaDetalleDto registrar(@Valid @RequestBody CrearEmergenciaRequest req) {
        return emergencias.registrar(sesion.usuario(), req);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR_MUNICIPAL', 'CCR', 'AUDITOR')")
    List<EmergenciaResumenDto> listar(@RequestParam(required = false) EstadoEmergencia estado) {
        return emergencias.listar(sesion.usuario(), estado);
    }

    @GetMapping("/{id}")
    EmergenciaDetalleDto detalle(@PathVariable Long id) {
        return emergencias.detalle(sesion.usuario(), id);
    }

    @PutMapping("/{id}/revision")
    @PreAuthorize("hasRole('CCR')")
    EmergenciaDetalleDto revisar(@PathVariable Long id, @Valid @RequestBody RevisionRequest req) {
        return emergencias.revisar(sesion.usuario(), id, req.observaciones());
    }

    /** Confirma los lotes y la duración de la ventana; completa la tarea de desglose. */
    @PostMapping("/{id}/convocatoria")
    @PreAuthorize("hasRole('CCR')")
    EmergenciaDetalleDto confirmarConvocatoria(@PathVariable Long id, @Valid @RequestBody ConvocatoriaRequest req) {
        return convocatorias.confirmarLotes(sesion.usuario(), id, req.horasVentana());
    }

    @GetMapping("/{id}/cobertura")
    @PreAuthorize("hasAnyRole('CCR', 'AUDITOR')")
    ResultadoCobertura cobertura(@PathVariable Long id) {
        acceso.visible(id, sesion.usuario());
        return cobertura.calcular(id);
    }

    @PostMapping("/{id}/decision-cobertura")
    @PreAuthorize("hasRole('CCR')")
    EmergenciaDetalleDto decidir(@PathVariable Long id, @Valid @RequestBody DecisionCoberturaRequest req) {
        return convocatorias.decidir(sesion.usuario(), id, req.decision(), req.horasVentana());
    }
}
