package ar.edu.unlp.dssd.rescuesync;

import ar.edu.unlp.dssd.rescuesync.emergencia.Emergencia;
import ar.edu.unlp.dssd.rescuesync.emergencia.Gravedad;
import ar.edu.unlp.dssd.rescuesync.emergencia.TipoDesastre;
import ar.edu.unlp.dssd.rescuesync.lote.Lote;
import ar.edu.unlp.dssd.rescuesync.lote.Prioridad;
import ar.edu.unlp.dssd.rescuesync.lote.TipoLote;
import ar.edu.unlp.dssd.rescuesync.organizacion.Organizacion;
import ar.edu.unlp.dssd.rescuesync.organizacion.TipoOrganizacion;
import ar.edu.unlp.dssd.rescuesync.usuario.Rol;
import ar.edu.unlp.dssd.rescuesync.usuario.Usuario;
import java.time.Instant;

/** Objetos de dominio en memoria para tests unitarios. */
public final class Fixtures {

    public static final Instant T0 = Instant.parse("2026-10-01T12:00:00Z");

    private Fixtures() {
    }

    public static Organizacion municipio() {
        return new Organizacion(TipoOrganizacion.MUNICIPIO, "Municipio", "30-1");
    }

    public static Organizacion ong(String nombre) {
        return new Organizacion(TipoOrganizacion.ONG, nombre, null);
    }

    public static Usuario usuario(Rol rol, Organizacion org) {
        return new Usuario("u-" + rol, "hash", "u@x", "Usuario " + rol, rol, org);
    }

    public static Emergencia emergencia() {
        Organizacion municipio = municipio();
        return new Emergencia(municipio, TipoDesastre.INUNDACION, Gravedad.ALTA, "Barrio Norte",
                "Desborde del arroyo con evacuados", usuario(Rol.OPERADOR_MUNICIPAL, municipio), T0);
    }

    /** Emergencia con la convocatoria abierta desde T0 durante {@code horas}. */
    public static Emergencia emergenciaConConvocatoriaAbierta(int horas) {
        Emergencia e = emergencia();
        e.revisar(usuario(Rol.CCR, new Organizacion(TipoOrganizacion.CCR, "CCR", null)), null, T0);
        e.definirConvocatoria(horas);
        e.abrirConvocatoria(T0);
        return e;
    }

    public static Lote lote(Emergencia e, String descripcion, int cantidad) {
        return new Lote(e, TipoLote.RECURSO, descripcion, "unidades", cantidad, Prioridad.MEDIA);
    }
}
