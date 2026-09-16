package ar.edu.unlp.dssd.rescuesync.organizacion;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Organizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoOrganizacion tipo;

    private String nombre;
    private String cuit;
    private String contacto;

    protected Organizacion() {
    }

    public Organizacion(TipoOrganizacion tipo, String nombre, String cuit) {
        this.tipo = tipo;
        this.nombre = nombre;
        this.cuit = cuit;
    }

    public Long getId() {
        return id;
    }

    public TipoOrganizacion getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCuit() {
        return cuit;
    }

    public String getContacto() {
        return contacto;
    }
}
