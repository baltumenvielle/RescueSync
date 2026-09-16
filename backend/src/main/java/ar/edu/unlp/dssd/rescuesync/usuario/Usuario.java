package ar.edu.unlp.dssd.rescuesync.usuario;

import ar.edu.unlp.dssd.rescuesync.organizacion.Organizacion;
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
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String passwordHash;
    private String email;
    private String nombre;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacion_id")
    private Organizacion organizacion;

    private String bonitaUsername;
    private Long bonitaUserId;
    private boolean activo = true;

    protected Usuario() {
    }

    public Usuario(String username, String passwordHash, String email, String nombre, Rol rol,
                   Organizacion organizacion) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.nombre = nombre;
        this.rol = rol;
        this.organizacion = organizacion;
        this.bonitaUsername = username;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public String getNombre() {
        return nombre;
    }

    public Rol getRol() {
        return rol;
    }

    public Organizacion getOrganizacion() {
        return organizacion;
    }

    public String getBonitaUsername() {
        return bonitaUsername;
    }

    public Long getBonitaUserId() {
        return bonitaUserId;
    }

    /** Lo completa la integración con Bonita al resolver el usuario espejo. */
    public void setBonitaUserId(Long bonitaUserId) {
        this.bonitaUserId = bonitaUserId;
    }

    public boolean isActivo() {
        return activo;
    }
}
