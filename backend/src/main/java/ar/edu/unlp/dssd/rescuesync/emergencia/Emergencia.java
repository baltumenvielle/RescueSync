package ar.edu.unlp.dssd.rescuesync.emergencia;

import ar.edu.unlp.dssd.rescuesync.common.ReglaNegocioException;
import ar.edu.unlp.dssd.rescuesync.organizacion.Organizacion;
import ar.edu.unlp.dssd.rescuesync.usuario.Usuario;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Duration;
import java.time.Instant;

@Entity
public class Emergencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "municipio_id")
    private Organizacion municipio;

    @Enumerated(EnumType.STRING)
    private TipoDesastre tipoDesastre;

    @Enumerated(EnumType.STRING)
    private Gravedad gravedad;

    private String zonaAfectada;
    private String descripcion;

    @Enumerated(EnumType.STRING)
    private EstadoEmergencia estado;

    private String caseIdBonita;

    private String observacionesRevision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revisada_por")
    private Usuario revisadaPor;

    private Instant fechaRevision;
    private Integer horasVentana;
    private Instant fechaAperturaConvocatoria;
    private Instant fechaCierreConvocatoria;

    @Enumerated(EnumType.STRING)
    private DecisionCcr decisionCcr;

    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by")
    private Usuario createdBy;

    protected Emergencia() {
    }

    public Emergencia(Organizacion municipio, TipoDesastre tipoDesastre, Gravedad gravedad, String zonaAfectada,
                      String descripcion, Usuario creador, Instant ahora) {
        this.municipio = municipio;
        this.tipoDesastre = tipoDesastre;
        this.gravedad = gravedad;
        this.zonaAfectada = zonaAfectada;
        this.descripcion = descripcion;
        this.createdBy = creador;
        this.createdAt = ahora;
        this.estado = EstadoEmergencia.REGISTRADA;
    }

    public void asociarCaso(String caseId) {
        if (caseIdBonita != null) {
            throw new ReglaNegocioException("La emergencia ya tiene un caso de proceso asociado");
        }
        this.caseIdBonita = caseId;
    }

    /** El CCR confirma la revisión de la información y pasa a definir los lotes. */
    public void revisar(Usuario ccr, String observaciones, Instant ahora) {
        exigirEstado(EstadoEmergencia.REGISTRADA, "revisar la emergencia");
        this.observacionesRevision = observaciones;
        this.revisadaPor = ccr;
        this.fechaRevision = ahora;
        this.estado = EstadoEmergencia.EN_REVISION;
    }

    public boolean admiteEdicionDeLotes() {
        return estado == EstadoEmergencia.EN_REVISION;
    }

    public void exigirEdicionDeLotes() {
        if (!admiteEdicionDeLotes()) {
            throw new ReglaNegocioException(
                    "Los lotes solo pueden modificarse durante el desglose (estado actual: " + estado + ")");
        }
    }

    /** Cierra el desglose: los lotes quedan definidos junto con la duración de la ventana. */
    public void definirConvocatoria(int horasVentana) {
        exigirEstado(EstadoEmergencia.EN_REVISION, "definir la convocatoria");
        validarHoras(horasVentana);
        this.horasVentana = horasVentana;
        this.estado = EstadoEmergencia.LOTES_DEFINIDOS;
    }

    /**
     * Publicación de la convocatoria (tarea automática del BPM). Es idempotente para tolerar
     * reintentos del conector: si ya está abierta no cambia las fechas.
     */
    public void abrirConvocatoria(Instant ahora) {
        if (estado == EstadoEmergencia.CONVOCATORIA_ABIERTA) {
            return;
        }
        exigirEstado(EstadoEmergencia.LOTES_DEFINIDOS, "publicar la convocatoria");
        this.fechaAperturaConvocatoria = ahora;
        this.fechaCierreConvocatoria = ahora.plus(Duration.ofHours(horasVentana));
        this.decisionCcr = null;
        this.estado = EstadoEmergencia.CONVOCATORIA_ABIERTA;
    }

    /** Vencimiento del timer de la ventana. Idempotente. */
    public void cerrarConvocatoria() {
        if (estado == EstadoEmergencia.CONVOCATORIA_ABIERTA) {
            this.estado = EstadoEmergencia.CONVOCATORIA_CERRADA;
        }
    }

    public boolean convocatoriaAbierta(Instant ahora) {
        return estado == EstadoEmergencia.CONVOCATORIA_ABIERTA
                && fechaCierreConvocatoria != null
                && !ahora.isAfter(fechaCierreConvocatoria);
    }

    /** Regla de validación temporal para toda alta o modificación de ofertas. */
    public void exigirConvocatoriaAbierta(Instant ahora) {
        if (estado != EstadoEmergencia.CONVOCATORIA_ABIERTA) {
            throw new ReglaNegocioException("La convocatoria de la emergencia " + id + " no está abierta");
        }
        if (fechaCierreConvocatoria == null || ahora.isAfter(fechaCierreConvocatoria)) {
            throw new ReglaNegocioException("La ventana de recepción de ofertas ya venció");
        }
    }

    /** Decisión del CCR ante cobertura insuficiente. */
    public void registrarDecision(DecisionCcr decision, Integer nuevasHorasVentana) {
        exigirEstado(EstadoEmergencia.CONVOCATORIA_CERRADA, "decidir sobre la cobertura");
        this.decisionCcr = decision;
        switch (decision) {
            case REABRIR -> {
                if (nuevasHorasVentana != null) {
                    validarHoras(nuevasHorasVentana);
                    this.horasVentana = nuevasHorasVentana;
                }
                this.estado = EstadoEmergencia.LOTES_DEFINIDOS;
            }
            case REFORMULAR -> this.estado = EstadoEmergencia.EN_REVISION;
            case CONTINUAR_PARCIAL -> this.estado = EstadoEmergencia.EN_VALIDACION;
        }
    }

    private static void validarHoras(int horas) {
        if (horas <= 0) {
            throw new ReglaNegocioException("La ventana de ofertas debe durar al menos una hora");
        }
    }

    private void exigirEstado(EstadoEmergencia esperado, String accion) {
        if (estado != esperado) {
            throw new ReglaNegocioException(
                    "No se puede " + accion + " en el estado " + estado + " (se esperaba " + esperado + ")");
        }
    }

    public Long getId() {
        return id;
    }

    public Organizacion getMunicipio() {
        return municipio;
    }

    public TipoDesastre getTipoDesastre() {
        return tipoDesastre;
    }

    public Gravedad getGravedad() {
        return gravedad;
    }

    public String getZonaAfectada() {
        return zonaAfectada;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public EstadoEmergencia getEstado() {
        return estado;
    }

    public String getCaseIdBonita() {
        return caseIdBonita;
    }

    public String getObservacionesRevision() {
        return observacionesRevision;
    }

    public Usuario getRevisadaPor() {
        return revisadaPor;
    }

    public Instant getFechaRevision() {
        return fechaRevision;
    }

    public Integer getHorasVentana() {
        return horasVentana;
    }

    public Instant getFechaAperturaConvocatoria() {
        return fechaAperturaConvocatoria;
    }

    public Instant getFechaCierreConvocatoria() {
        return fechaCierreConvocatoria;
    }

    public DecisionCcr getDecisionCcr() {
        return decisionCcr;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Usuario getCreatedBy() {
        return createdBy;
    }
}
