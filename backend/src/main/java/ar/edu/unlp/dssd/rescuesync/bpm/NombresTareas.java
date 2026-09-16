package ar.edu.unlp.dssd.rescuesync.bpm;

import ar.edu.unlp.dssd.rescuesync.config.RescueSyncProperties;
import java.text.Normalizer;
import java.util.Locale;
import org.springframework.stereotype.Component;

/** Reconoce las tareas del modelo por nombre, ignorando mayúsculas, tildes y espacios extra. */
@Component
public class NombresTareas {

    private final RescueSyncProperties.Tareas tareas;

    public NombresTareas(RescueSyncProperties properties) {
        this.tareas = properties.bpm().tareas();
    }

    public String nombreDe(AccionTarea accion) {
        return switch (accion) {
            case REGISTRO -> tareas.registro();
            case REVISION -> tareas.revision();
            case DESGLOSE -> tareas.desglose();
            case EVALUAR_COBERTURA -> tareas.evaluarCobertura();
            case OTRA -> throw new IllegalArgumentException("OTRA no corresponde a una tarea concreta");
        };
    }

    public AccionTarea accionDe(String nombreTarea) {
        for (AccionTarea accion : AccionTarea.values()) {
            if (accion != AccionTarea.OTRA && coincide(nombreTarea, nombreDe(accion))) {
                return accion;
            }
        }
        return AccionTarea.OTRA;
    }

    public boolean es(String nombreTarea, AccionTarea accion) {
        return accion != AccionTarea.OTRA && coincide(nombreTarea, nombreDe(accion));
    }

    static boolean coincide(String a, String b) {
        return a != null && b != null && normalizar(a).equals(normalizar(b));
    }

    static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", " ");
    }
}
