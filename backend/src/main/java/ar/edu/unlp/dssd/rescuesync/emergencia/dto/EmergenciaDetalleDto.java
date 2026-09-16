package ar.edu.unlp.dssd.rescuesync.emergencia.dto;

import ar.edu.unlp.dssd.rescuesync.emergencia.DecisionCcr;
import ar.edu.unlp.dssd.rescuesync.emergencia.Emergencia;
import ar.edu.unlp.dssd.rescuesync.emergencia.EstadoEmergencia;
import ar.edu.unlp.dssd.rescuesync.emergencia.Gravedad;
import ar.edu.unlp.dssd.rescuesync.emergencia.TipoDesastre;
import ar.edu.unlp.dssd.rescuesync.lote.dto.LoteDto;
import ar.edu.unlp.dssd.rescuesync.organizacion.OrganizacionDto;
import java.time.Instant;
import java.util.List;

public record EmergenciaDetalleDto(Long id, TipoDesastre tipoDesastre, Gravedad gravedad, String zonaAfectada,
                                   String descripcion, EstadoEmergencia estado, OrganizacionDto municipio,
                                   String caseId, Instant createdAt, String observacionesRevision,
                                   String revisadaPor, Instant fechaRevision, Integer horasVentana,
                                   Instant fechaAperturaConvocatoria, Instant fechaCierreConvocatoria,
                                   DecisionCcr decisionCcr, boolean admiteEdicionDeLotes, List<LoteDto> lotes) {

    public static EmergenciaDetalleDto de(Emergencia e, List<LoteDto> lotes) {
        return new EmergenciaDetalleDto(e.getId(), e.getTipoDesastre(), e.getGravedad(), e.getZonaAfectada(),
                e.getDescripcion(), e.getEstado(), OrganizacionDto.de(e.getMunicipio()), e.getCaseIdBonita(),
                e.getCreatedAt(), e.getObservacionesRevision(),
                e.getRevisadaPor() != null ? e.getRevisadaPor().getNombre() : null, e.getFechaRevision(),
                e.getHorasVentana(), e.getFechaAperturaConvocatoria(), e.getFechaCierreConvocatoria(),
                e.getDecisionCcr(), e.admiteEdicionDeLotes(), lotes);
    }
}
