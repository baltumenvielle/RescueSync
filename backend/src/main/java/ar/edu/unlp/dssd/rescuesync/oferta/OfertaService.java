package ar.edu.unlp.dssd.rescuesync.oferta;

import ar.edu.unlp.dssd.rescuesync.auditoria.AuditoriaService;
import ar.edu.unlp.dssd.rescuesync.common.AccesoDenegadoException;
import ar.edu.unlp.dssd.rescuesync.common.NoEncontradoException;
import ar.edu.unlp.dssd.rescuesync.common.ReglaNegocioException;
import ar.edu.unlp.dssd.rescuesync.emergencia.AccesoEmergencias;
import ar.edu.unlp.dssd.rescuesync.emergencia.Emergencia;
import ar.edu.unlp.dssd.rescuesync.emergencia.EmergenciaRepository;
import ar.edu.unlp.dssd.rescuesync.emergencia.EstadoEmergencia;
import ar.edu.unlp.dssd.rescuesync.emergencia.dto.EmergenciaResumenDto;
import ar.edu.unlp.dssd.rescuesync.lote.Lote;
import ar.edu.unlp.dssd.rescuesync.lote.LoteRepository;
import ar.edu.unlp.dssd.rescuesync.lote.dto.LoteDto;
import ar.edu.unlp.dssd.rescuesync.oferta.dto.ConvocatoriaDto;
import ar.edu.unlp.dssd.rescuesync.oferta.dto.OfertaConsolidadaDto;
import ar.edu.unlp.dssd.rescuesync.oferta.dto.OfertaDto;
import ar.edu.unlp.dssd.rescuesync.oferta.dto.OfertaRequest;
import ar.edu.unlp.dssd.rescuesync.oferta.dto.VersionOfertaDto;
import ar.edu.unlp.dssd.rescuesync.seguridad.UsuarioActual;
import ar.edu.unlp.dssd.rescuesync.usuario.Rol;
import ar.edu.unlp.dssd.rescuesync.usuario.Usuario;
import ar.edu.unlp.dssd.rescuesync.usuario.UsuarioService;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ofertas de ayuda de las ONGs: alta, edición versionada y envío dentro de la ventana. */
@Service
public class OfertaService {

    private final OfertaRepository ofertas;
    private final EmergenciaRepository emergencias;
    private final LoteRepository lotes;
    private final AccesoEmergencias acceso;
    private final UsuarioService usuarios;
    private final AuditoriaService auditoria;
    private final Clock clock;

    public OfertaService(OfertaRepository ofertas, EmergenciaRepository emergencias, LoteRepository lotes,
                         AccesoEmergencias acceso, UsuarioService usuarios, AuditoriaService auditoria, Clock clock) {
        this.ofertas = ofertas;
        this.emergencias = emergencias;
        this.lotes = lotes;
        this.acceso = acceso;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ConvocatoriaDto> convocatoriasAbiertas(UsuarioActual actual) {
        return emergencias.findByEstadoAndFechaCierreConvocatoriaAfterOrderByFechaCierreConvocatoriaAsc(
                        EstadoEmergencia.CONVOCATORIA_ABIERTA, clock.instant()).stream()
                .map(e -> new ConvocatoriaDto(EmergenciaResumenDto.de(e), e.getDescripcion(),
                        e.getFechaAperturaConvocatoria(), e.getFechaCierreConvocatoria(),
                        lotes.findByEmergenciaIdOrderById(e.getId()).stream().map(LoteDto::de).toList(),
                        ofertas.findByEmergenciaIdAndOngLiderId(e.getId(), actual.organizacionId())
                                .map(o -> new ConvocatoriaDto.MiOferta(o.getId(), o.getEstado(), o.getVersionActual()))
                                .orElse(null)))
                .toList();
    }

    @Transactional
    public OfertaDto crear(UsuarioActual actual, Long emergenciaId, OfertaRequest req) {
        Usuario autor = usuarios.obtener(actual.id());
        Emergencia emergencia = bloquearEmergencia(emergenciaId);
        Instant ahora = clock.instant();
        emergencia.exigirConvocatoriaAbierta(ahora);
        if (ofertas.existsByEmergenciaIdAndOngLiderId(emergenciaId, actual.organizacionId())) {
            throw new ReglaNegocioException(
                    "Su organización ya tiene una oferta para esta emergencia; edítela para generar una nueva versión");
        }
        Oferta oferta = ofertas.save(Oferta.crear(emergencia, autor.getOrganizacion(), autor,
                comentario(req), items(emergencia, req), ahora));
        auditoria.registrar("OFERTA", oferta.getId(), "CREADA", autor.getId(), resumen(oferta));
        return OfertaDto.de(oferta, true);
    }

    /** Genera una nueva versión de la oferta; las anteriores quedan intactas. */
    @Transactional
    public OfertaDto editar(UsuarioActual actual, Long ofertaId, OfertaRequest req) {
        Usuario autor = usuarios.obtener(actual.id());
        Oferta oferta = propia(actual, ofertaId);
        Emergencia emergencia = bloquearEmergencia(oferta.getEmergencia().getId());
        Instant ahora = clock.instant();
        emergencia.exigirConvocatoriaAbierta(ahora);
        oferta.nuevaVersion(autor, comentario(req), items(emergencia, req), ahora);
        ofertas.flush();
        auditoria.registrar("OFERTA", ofertaId, "NUEVA_VERSION", autor.getId(), resumen(oferta));
        return OfertaDto.de(oferta, true);
    }

    @Transactional
    public OfertaDto enviar(UsuarioActual actual, Long ofertaId) {
        Oferta oferta = propia(actual, ofertaId);
        Emergencia emergencia = bloquearEmergencia(oferta.getEmergencia().getId());
        emergencia.exigirConvocatoriaAbierta(clock.instant());
        oferta.enviar();
        auditoria.registrar("OFERTA", ofertaId, "ENVIADA", actual.id(), resumen(oferta));
        return OfertaDto.de(oferta, true);
    }

    @Transactional(readOnly = true)
    public OfertaDto detalle(UsuarioActual actual, Long ofertaId) {
        Oferta oferta = visible(actual, ofertaId);
        return OfertaDto.de(oferta, esEditable(oferta));
    }

    @Transactional(readOnly = true)
    public List<VersionOfertaDto> versiones(UsuarioActual actual, Long ofertaId) {
        return visible(actual, ofertaId).getVersiones().stream().map(VersionOfertaDto::de).toList();
    }

    @Transactional(readOnly = true)
    public List<OfertaDto> misOfertas(UsuarioActual actual) {
        return ofertas.findByOngLiderIdOrderByCreatedAtDesc(actual.organizacionId()).stream()
                .map(o -> OfertaDto.de(o, esEditable(o)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OfertaDto> deEmergencia(UsuarioActual actual, Long emergenciaId) {
        acceso.visible(emergenciaId, actual);
        return ofertas.findByEmergenciaIdOrderById(emergenciaId).stream()
                .map(o -> OfertaDto.de(o, false))
                .toList();
    }

    /** Ofertas enviadas de la emergencia en su versión vigente, para el conector del BPM. */
    @Transactional(readOnly = true)
    public List<OfertaConsolidadaDto> consolidadas(Long emergenciaId) {
        if (!emergencias.existsById(emergenciaId)) {
            throw new NoEncontradoException("Emergencia", emergenciaId);
        }
        return ofertas.findByEmergenciaIdAndEstadoIn(emergenciaId, List.of(EstadoOferta.ENVIADA)).stream()
                .map(OfertaConsolidadaDto::de)
                .toList();
    }

    private boolean esEditable(Oferta oferta) {
        return (oferta.getEstado() == EstadoOferta.BORRADOR || oferta.getEstado() == EstadoOferta.ENVIADA)
                && oferta.getEmergencia().convocatoriaAbierta(clock.instant());
    }

    private Oferta propia(UsuarioActual actual, Long ofertaId) {
        Oferta oferta = ofertas.findDetalleById(ofertaId)
                .orElseThrow(() -> new NoEncontradoException("Oferta", ofertaId));
        if (!oferta.perteneceA(actual.organizacionId())) {
            throw new AccesoDenegadoException("La oferta pertenece a otra organización");
        }
        return oferta;
    }

    private Oferta visible(UsuarioActual actual, Long ofertaId) {
        if (actual.es(Rol.REPRESENTANTE_ONG)) {
            return propia(actual, ofertaId);
        }
        if (actual.es(Rol.CCR) || actual.es(Rol.AUDITOR)) {
            return ofertas.findDetalleById(ofertaId).orElseThrow(() -> new NoEncontradoException("Oferta", ofertaId));
        }
        throw new AccesoDenegadoException("El perfil no tiene acceso a las ofertas");
    }

    private Emergencia bloquearEmergencia(Long id) {
        return emergencias.findParaActualizar(id).orElseThrow(() -> new NoEncontradoException("Emergencia", id));
    }

    private List<ItemSolicitado> items(Emergencia emergencia, OfertaRequest req) {
        Map<Long, Lote> lotesEmergencia = lotes.findByEmergenciaIdOrderById(emergencia.getId()).stream()
                .collect(Collectors.toMap(Lote::getId, Function.identity()));
        return req.items().stream()
                .map(item -> {
                    Lote lote = lotesEmergencia.get(item.loteId());
                    if (lote == null) {
                        throw new ReglaNegocioException(
                                "El lote " + item.loteId() + " no pertenece a la emergencia " + emergencia.getId());
                    }
                    return new ItemSolicitado(lote, item.cantidad());
                })
                .toList();
    }

    private static String comentario(OfertaRequest req) {
        return req.comentario() == null || req.comentario().isBlank() ? null : req.comentario().trim();
    }

    private static Map<String, Object> resumen(Oferta oferta) {
        return Map.of(
                "emergenciaId", oferta.getEmergencia().getId(),
                "estado", oferta.getEstado(),
                "version", oferta.getVersionActual(),
                "items", oferta.versionVigente().getItems().stream()
                        .map(i -> Map.of("loteId", i.getLote().getId(), "cantidad", i.getCantidadOfrecida()))
                        .toList());
    }
}
