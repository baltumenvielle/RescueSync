package ar.edu.unlp.dssd.rescuesync.oferta;

import ar.edu.unlp.dssd.rescuesync.common.ReglaNegocioException;
import ar.edu.unlp.dssd.rescuesync.emergencia.Emergencia;
import ar.edu.unlp.dssd.rescuesync.lote.Lote;
import ar.edu.unlp.dssd.rescuesync.organizacion.Organizacion;
import ar.edu.unlp.dssd.rescuesync.usuario.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Oferta de ayuda de una ONG para una emergencia.
 *
 * <p>Trazabilidad: los ítems nunca se modifican en el lugar. Cada edición crea una nueva
 * {@link OfertaVersion} con la foto completa de los ítems y avanza {@code versionActual}.
 */
@Entity
public class Oferta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emergencia_id")
    private Emergencia emergencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ong_lider_id")
    private Organizacion ongLider;

    @Enumerated(EnumType.STRING)
    private EstadoOferta estado;

    private int versionActual;

    private Instant createdAt;

    @OneToMany(mappedBy = "oferta", cascade = CascadeType.ALL)
    @OrderBy("numero ASC")
    private List<OfertaVersion> versiones = new ArrayList<>();

    protected Oferta() {
    }

    private Oferta(Emergencia emergencia, Organizacion ongLider, Instant ahora) {
        this.emergencia = emergencia;
        this.ongLider = ongLider;
        this.estado = EstadoOferta.BORRADOR;
        this.versionActual = 0;
        this.createdAt = ahora;
    }

    public static Oferta crear(Emergencia emergencia, Organizacion ong, Usuario autor, String comentario,
                               List<ItemSolicitado> items, Instant ahora) {
        Oferta oferta = new Oferta(emergencia, ong, ahora);
        oferta.nuevaVersion(autor, comentario, items, ahora);
        return oferta;
    }

    /** Registra una nueva versión con el detalle completo de ítems. Las anteriores quedan intactas. */
    public OfertaVersion nuevaVersion(Usuario autor, String comentario, List<ItemSolicitado> items, Instant ahora) {
        exigirEditable();
        validarItems(items);
        OfertaVersion version = new OfertaVersion(this, versionActual + 1, autor, comentario, ahora);
        items.forEach(item -> version.agregarItem(item.lote(), item.cantidad()));
        versiones.add(version);
        versionActual = version.getNumero();
        return version;
    }

    /** La ONG confirma la oferta: pasa a considerarse en el cálculo de cobertura. */
    public void enviar() {
        if (estado == EstadoOferta.ENVIADA) {
            throw new ReglaNegocioException("La oferta ya fue enviada");
        }
        exigirEditable();
        estado = EstadoOferta.ENVIADA;
    }

    public OfertaVersion versionVigente() {
        return versiones.stream()
                .filter(v -> v.getNumero() == versionActual)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Oferta " + id + " sin versión " + versionActual));
    }

    public boolean perteneceA(Long organizacionId) {
        return ongLider.getId() != null && ongLider.getId().equals(organizacionId);
    }

    private void exigirEditable() {
        if (estado != EstadoOferta.BORRADOR && estado != EstadoOferta.ENVIADA) {
            throw new ReglaNegocioException("La oferta ya no puede modificarse (estado " + estado + ")");
        }
    }

    private void validarItems(List<ItemSolicitado> items) {
        if (items == null || items.isEmpty()) {
            throw new ReglaNegocioException("La oferta debe incluir al menos un lote");
        }
        Set<Object> lotesVistos = new HashSet<>();
        for (ItemSolicitado item : items) {
            Lote lote = item.lote();
            if (!lote.perteneceA(emergencia)) {
                throw new ReglaNegocioException("El lote " + lote.getId() + " no pertenece a la emergencia");
            }
            if (item.cantidad() <= 0) {
                throw new ReglaNegocioException("La cantidad ofrecida para cada lote debe ser mayor a cero");
            }
            Object clave = lote.getId() != null ? lote.getId() : lote;
            if (!lotesVistos.add(clave)) {
                throw new ReglaNegocioException("El lote " + lote.getDescripcion() + " figura más de una vez");
            }
        }
    }

    public Long getId() {
        return id;
    }

    public Emergencia getEmergencia() {
        return emergencia;
    }

    public Organizacion getOngLider() {
        return ongLider;
    }

    public EstadoOferta getEstado() {
        return estado;
    }

    public int getVersionActual() {
        return versionActual;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OfertaVersion> getVersiones() {
        return Collections.unmodifiableList(versiones);
    }
}
