package ar.edu.unlp.dssd.rescuesync.cobertura;

import static ar.edu.unlp.dssd.rescuesync.Fixtures.T0;
import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.unlp.dssd.rescuesync.Fixtures;
import ar.edu.unlp.dssd.rescuesync.emergencia.Emergencia;
import ar.edu.unlp.dssd.rescuesync.lote.Lote;
import ar.edu.unlp.dssd.rescuesync.oferta.ItemSolicitado;
import ar.edu.unlp.dssd.rescuesync.oferta.Oferta;
import ar.edu.unlp.dssd.rescuesync.organizacion.Organizacion;
import ar.edu.unlp.dssd.rescuesync.usuario.Rol;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CalculadoraCoberturaTest {

    private Emergencia emergencia;
    private Lote paramedicos;
    private Lote raciones;

    @BeforeEach
    void setUp() {
        emergencia = Fixtures.emergenciaConConvocatoriaAbierta(24);
        paramedicos = Fixtures.lote(emergencia, "Paramédicos", 5);
        raciones = Fixtures.lote(emergencia, "Raciones", 1000);
    }

    @Test
    void sinOfertasLaCoberturaEsIncompleta() {
        ResultadoCobertura r = CalculadoraCobertura.calcular(List.of(paramedicos, raciones), List.of());

        assertThat(r.completa()).isFalse();
        assertThat(r.lotesCubiertos()).isZero();
        assertThat(r.lotes()).extracting(CoberturaLote::cantidadOfrecida).containsExactly(0, 0);
    }

    @Test
    void sinLotesNoSeConsideraCompleta() {
        assertThat(CalculadoraCobertura.calcular(List.of(), List.of()).completa()).isFalse();
    }

    @Test
    void sumaOfertasParcialesDeVariasOngs() {
        Oferta a = enviada("A", new ItemSolicitado(paramedicos, 3), new ItemSolicitado(raciones, 600));
        Oferta b = enviada("B", new ItemSolicitado(paramedicos, 2), new ItemSolicitado(raciones, 400));

        ResultadoCobertura r = CalculadoraCobertura.calcular(List.of(paramedicos, raciones), List.of(a, b));

        assertThat(r.completa()).isTrue();
        assertThat(r.lotesCubiertos()).isEqualTo(2);
        assertThat(r.lotes().getFirst().ofertasQueAportan()).isEqualTo(2);
    }

    @Test
    void esIncompletaSiAlgunLoteNoAlcanzaLaCantidad() {
        Oferta a = enviada("A", new ItemSolicitado(paramedicos, 10), new ItemSolicitado(raciones, 999));

        ResultadoCobertura r = CalculadoraCobertura.calcular(List.of(paramedicos, raciones), List.of(a));

        assertThat(r.completa()).isFalse();
        assertThat(r.lotesCubiertos()).isEqualTo(1);
        CoberturaLote lotesRaciones = r.lotes().get(1);
        assertThat(lotesRaciones.cubierto()).isFalse();
        assertThat(lotesRaciones.porcentaje()).isEqualTo(99);
        assertThat(r.lotes().getFirst().porcentaje()).isEqualTo(100);
    }

    @Test
    void ignoraOfertasEnBorrador() {
        Oferta borrador = oferta("A", new ItemSolicitado(paramedicos, 5), new ItemSolicitado(raciones, 1000));

        assertThat(CalculadoraCobertura.calcular(List.of(paramedicos, raciones), List.of(borrador)).completa())
                .isFalse();
    }

    @Test
    void usaSoloLaVersionVigenteDeCadaOferta() {
        Oferta a = enviada("A", new ItemSolicitado(paramedicos, 5), new ItemSolicitado(raciones, 1000));
        a.nuevaVersion(Fixtures.usuario(Rol.REPRESENTANTE_ONG, a.getOngLider()), "baja",
                List.of(new ItemSolicitado(paramedicos, 1)), T0);

        ResultadoCobertura r = CalculadoraCobertura.calcular(List.of(paramedicos, raciones), List.of(a));

        assertThat(r.completa()).isFalse();
        assertThat(r.lotes()).extracting(CoberturaLote::cantidadOfrecida).containsExactly(1, 0);
    }

    private Oferta enviada(String ong, ItemSolicitado... items) {
        Oferta o = oferta(ong, items);
        o.enviar();
        return o;
    }

    private Oferta oferta(String nombreOng, ItemSolicitado... items) {
        Organizacion ong = Fixtures.ong(nombreOng);
        return Oferta.crear(emergencia, ong, Fixtures.usuario(Rol.REPRESENTANTE_ONG, ong), null, List.of(items), T0);
    }
}
