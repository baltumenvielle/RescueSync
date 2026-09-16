package ar.edu.unlp.dssd.rescuesync.oferta.dto;

import ar.edu.unlp.dssd.rescuesync.emergencia.dto.EmergenciaResumenDto;
import ar.edu.unlp.dssd.rescuesync.lote.dto.LoteDto;
import ar.edu.unlp.dssd.rescuesync.oferta.EstadoOferta;
import java.time.Instant;
import java.util.List;

/** Convocatoria abierta vista por una ONG, con el resumen de su propia oferta si ya cargó una. */
public record ConvocatoriaDto(EmergenciaResumenDto emergencia, String descripcion, Instant fechaApertura,
                              Instant fechaCierre, List<LoteDto> lotes, MiOferta miOferta) {

    public record MiOferta(Long id, EstadoOferta estado, int versionActual) {
    }
}
