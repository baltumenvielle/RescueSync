package ar.edu.unlp.dssd.rescuesync.oferta;

import ar.edu.unlp.dssd.rescuesync.lote.Lote;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class OfertaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oferta_version_id")
    private OfertaVersion version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id")
    private Lote lote;

    private int cantidadOfrecida;

    /** Lo asigna la validación externa (Sistema Nacional); nulo hasta entonces. */
    @Enumerated(EnumType.STRING)
    private RolItem rol;

    protected OfertaItem() {
    }

    OfertaItem(OfertaVersion version, Lote lote, int cantidadOfrecida) {
        this.version = version;
        this.lote = lote;
        this.cantidadOfrecida = cantidadOfrecida;
    }

    public Long getId() {
        return id;
    }

    public OfertaVersion getVersion() {
        return version;
    }

    public Lote getLote() {
        return lote;
    }

    public int getCantidadOfrecida() {
        return cantidadOfrecida;
    }

    public RolItem getRol() {
        return rol;
    }
}
