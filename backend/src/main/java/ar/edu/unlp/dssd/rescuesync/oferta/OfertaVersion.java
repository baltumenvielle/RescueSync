package ar.edu.unlp.dssd.rescuesync.oferta;

import ar.edu.unlp.dssd.rescuesync.lote.Lote;
import ar.edu.unlp.dssd.rescuesync.usuario.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
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
import java.util.List;

/** Foto inmutable del detalle de una oferta en un momento dado. */
@Entity
public class OfertaVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oferta_id")
    private Oferta oferta;

    private int numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    private String comentario;
    private Instant createdAt;

    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private List<OfertaItem> items = new ArrayList<>();

    protected OfertaVersion() {
    }

    OfertaVersion(Oferta oferta, int numero, Usuario autor, String comentario, Instant ahora) {
        this.oferta = oferta;
        this.numero = numero;
        this.autor = autor;
        this.comentario = comentario;
        this.createdAt = ahora;
    }

    void agregarItem(Lote lote, int cantidad) {
        items.add(new OfertaItem(this, lote, cantidad));
    }

    public int cantidadPara(Lote lote) {
        return items.stream()
                .filter(i -> i.getLote() == lote || (lote.getId() != null && lote.getId().equals(i.getLote().getId())))
                .mapToInt(OfertaItem::getCantidadOfrecida)
                .sum();
    }

    public Long getId() {
        return id;
    }

    public Oferta getOferta() {
        return oferta;
    }

    public int getNumero() {
        return numero;
    }

    public Usuario getAutor() {
        return autor;
    }

    public String getComentario() {
        return comentario;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OfertaItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
