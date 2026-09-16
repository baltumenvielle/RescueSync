package ar.edu.unlp.dssd.rescuesync.oferta;

import static ar.edu.unlp.dssd.rescuesync.Fixtures.T0;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.unlp.dssd.rescuesync.Fixtures;
import ar.edu.unlp.dssd.rescuesync.common.ReglaNegocioException;
import ar.edu.unlp.dssd.rescuesync.emergencia.Emergencia;
import ar.edu.unlp.dssd.rescuesync.lote.Lote;
import ar.edu.unlp.dssd.rescuesync.organizacion.Organizacion;
import ar.edu.unlp.dssd.rescuesync.usuario.Rol;
import ar.edu.unlp.dssd.rescuesync.usuario.Usuario;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OfertaVersionadoTest {

    private Emergencia emergencia;
    private Lote paramedicos;
    private Lote raciones;
    private Organizacion ong;
    private Usuario autor;

    @BeforeEach
    void setUp() {
        emergencia = Fixtures.emergenciaConConvocatoriaAbierta(48);
        paramedicos = Fixtures.lote(emergencia, "Paramédicos", 5);
        raciones = Fixtures.lote(emergencia, "Raciones", 1000);
        ong = Fixtures.ong("Cruz Roja");
        autor = Fixtures.usuario(Rol.REPRESENTANTE_ONG, ong);
    }

    @Test
    void crearGeneraLaVersionUnoEnBorrador() {
        Oferta oferta = Oferta.crear(emergencia, ong, autor, "inicial",
                List.of(new ItemSolicitado(paramedicos, 2)), T0);

        assertThat(oferta.getEstado()).isEqualTo(EstadoOferta.BORRADOR);
        assertThat(oferta.getVersionActual()).isEqualTo(1);
        assertThat(oferta.getVersiones()).hasSize(1);
        assertThat(oferta.versionVigente().cantidadPara(paramedicos)).isEqualTo(2);
    }

    @Test
    void editarCreaNuevaVersionSinModificarLasAnteriores() {
        Oferta oferta = Oferta.crear(emergencia, ong, autor, "v1", List.of(new ItemSolicitado(paramedicos, 2)), T0);
        OfertaVersion v1 = oferta.versionVigente();

        oferta.nuevaVersion(autor, "v2", List.of(new ItemSolicitado(paramedicos, 4), new ItemSolicitado(raciones, 300)),
                T0.plusSeconds(60));

        assertThat(oferta.getVersionActual()).isEqualTo(2);
        assertThat(oferta.getVersiones()).extracting(OfertaVersion::getNumero).containsExactly(1, 2);
        assertThat(oferta.versionVigente().getComentario()).isEqualTo("v2");
        assertThat(oferta.versionVigente().cantidadPara(paramedicos)).isEqualTo(4);
        assertThat(oferta.versionVigente().cantidadPara(raciones)).isEqualTo(300);
        // la versión 1 queda intacta
        assertThat(v1.getItems()).hasSize(1);
        assertThat(v1.cantidadPara(paramedicos)).isEqualTo(2);
        assertThat(v1.cantidadPara(raciones)).isZero();
    }

    @Test
    void permiteOfertasParcialesSobreUnSubconjuntoDeLotes() {
        Oferta oferta = Oferta.crear(emergencia, ong, autor, null, List.of(new ItemSolicitado(raciones, 10)), T0);

        assertThat(oferta.versionVigente().getItems()).hasSize(1);
        assertThat(oferta.versionVigente().cantidadPara(paramedicos)).isZero();
    }

    @Test
    void editarUnaOfertaEnviadaLaMantieneEnviada() {
        Oferta oferta = Oferta.crear(emergencia, ong, autor, null, List.of(new ItemSolicitado(raciones, 10)), T0);
        oferta.enviar();

        oferta.nuevaVersion(autor, null, List.of(new ItemSolicitado(raciones, 20)), T0);

        assertThat(oferta.getEstado()).isEqualTo(EstadoOferta.ENVIADA);
        assertThat(oferta.getVersionActual()).isEqualTo(2);
    }

    @Test
    void noPermiteEnviarDosVeces() {
        Oferta oferta = Oferta.crear(emergencia, ong, autor, null, List.of(new ItemSolicitado(raciones, 10)), T0);
        oferta.enviar();

        assertThatThrownBy(oferta::enviar).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void rechazaItemsVacios() {
        assertThatThrownBy(() -> Oferta.crear(emergencia, ong, autor, null, List.of(), T0))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("al menos un lote");
    }

    @Test
    void rechazaLotesRepetidos() {
        List<ItemSolicitado> items = List.of(new ItemSolicitado(raciones, 10), new ItemSolicitado(raciones, 5));

        assertThatThrownBy(() -> Oferta.crear(emergencia, ong, autor, null, items, T0))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("más de una vez");
    }

    @Test
    void rechazaCantidadesNoPositivas() {
        assertThatThrownBy(() -> Oferta.crear(emergencia, ong, autor, null,
                List.of(new ItemSolicitado(raciones, 0)), T0))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void rechazaLotesDeOtraEmergencia() {
        Lote ajeno = Fixtures.lote(Fixtures.emergenciaConConvocatoriaAbierta(10), "Otro", 1);

        assertThatThrownBy(() -> Oferta.crear(emergencia, ong, autor, null, List.of(new ItemSolicitado(ajeno, 1)), T0))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no pertenece");
    }

    @Test
    void unaVersionInvalidaNoAvanzaLaVersionActual() {
        Oferta oferta = Oferta.crear(emergencia, ong, autor, null, List.of(new ItemSolicitado(raciones, 10)), T0);

        assertThatThrownBy(() -> oferta.nuevaVersion(autor, null, List.of(), T0))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(oferta.getVersionActual()).isEqualTo(1);
        assertThat(oferta.getVersiones()).hasSize(1);
    }
}
