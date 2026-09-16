package ar.edu.unlp.dssd.rescuesync.auditoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
import org.hibernate.annotations.ColumnTransformer;

@Entity
public class EventoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entidad;
    private Long entidadId;
    private String accion;
    private Long usuarioId;

    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String payload;

    private Instant timestamp;

    protected EventoAuditoria() {
    }

    public EventoAuditoria(String entidad, Long entidadId, String accion, Long usuarioId, String payload,
                           Instant timestamp) {
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.accion = accion;
        this.usuarioId = usuarioId;
        this.payload = payload;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getEntidad() {
        return entidad;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public String getAccion() {
        return accion;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
