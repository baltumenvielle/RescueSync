package ar.edu.unlp.dssd.rescuesync.lote;

import ar.edu.unlp.dssd.rescuesync.emergencia.Emergencia;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** Lote de necesidades de una emergencia (ej: 5 paramédicos, 1000 raciones). */
@Entity
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emergencia_id")
    private Emergencia emergencia;

    @Enumerated(EnumType.STRING)
    private TipoLote tipo;

    private String descripcion;
    private String unidad;
    private int cantidadRequerida;

    @Enumerated(EnumType.STRING)
    private Prioridad prioridad;

    protected Lote() {
    }

    public Lote(Emergencia emergencia, TipoLote tipo, String descripcion, String unidad, int cantidadRequerida,
                Prioridad prioridad) {
        this.emergencia = emergencia;
        actualizar(tipo, descripcion, unidad, cantidadRequerida, prioridad);
    }

    public void actualizar(TipoLote tipo, String descripcion, String unidad, int cantidadRequerida,
                           Prioridad prioridad) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.unidad = unidad;
        this.cantidadRequerida = cantidadRequerida;
        this.prioridad = prioridad;
    }

    public boolean perteneceA(Emergencia otra) {
        return emergencia == otra || (emergencia.getId() != null && emergencia.getId().equals(otra.getId()));
    }

    public Long getId() {
        return id;
    }

    public Emergencia getEmergencia() {
        return emergencia;
    }

    public TipoLote getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getUnidad() {
        return unidad;
    }

    public int getCantidadRequerida() {
        return cantidadRequerida;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }
}
