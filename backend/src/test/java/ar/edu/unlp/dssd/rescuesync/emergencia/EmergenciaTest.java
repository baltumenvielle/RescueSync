package ar.edu.unlp.dssd.rescuesync.emergencia;

import static ar.edu.unlp.dssd.rescuesync.Fixtures.T0;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.unlp.dssd.rescuesync.Fixtures;
import ar.edu.unlp.dssd.rescuesync.common.ReglaNegocioException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EmergenciaTest {

    @Test
    void recorreRevisionDesgloseYPublicacion() {
        Emergencia e = Fixtures.emergencia();
        assertThat(e.getEstado()).isEqualTo(EstadoEmergencia.REGISTRADA);
        assertThat(e.admiteEdicionDeLotes()).isFalse();

        e.revisar(null, "ok", T0);
        assertThat(e.getEstado()).isEqualTo(EstadoEmergencia.EN_REVISION);
        assertThat(e.admiteEdicionDeLotes()).isTrue();

        e.definirConvocatoria(48);
        assertThat(e.getEstado()).isEqualTo(EstadoEmergencia.LOTES_DEFINIDOS);

        e.abrirConvocatoria(T0);
        assertThat(e.getEstado()).isEqualTo(EstadoEmergencia.CONVOCATORIA_ABIERTA);
        assertThat(e.getFechaCierreConvocatoria()).isEqualTo(T0.plus(Duration.ofHours(48)));
    }

    @Test
    void publicarEsIdempotenteYNoCorreLaVentana() {
        Emergencia e = Fixtures.emergenciaConConvocatoriaAbierta(10);

        e.abrirConvocatoria(T0.plus(Duration.ofHours(5)));

        assertThat(e.getFechaCierreConvocatoria()).isEqualTo(T0.plus(Duration.ofHours(10)));
    }

    @Test
    void aceptaOfertasHastaElInstanteDeCierreInclusive() {
        Emergencia e = Fixtures.emergenciaConConvocatoriaAbierta(2);

        assertThatCode(() -> e.exigirConvocatoriaAbierta(T0.plus(Duration.ofHours(2)))).doesNotThrowAnyException();
        assertThat(e.convocatoriaAbierta(T0.plusSeconds(1))).isTrue();
    }

    @Test
    void rechazaOfertasDespuesDelCierreAunqueElEstadoSigaAbierto() {
        Emergencia e = Fixtures.emergenciaConConvocatoriaAbierta(2);
        var despues = T0.plus(Duration.ofHours(2)).plusSeconds(1);

        assertThat(e.getEstado()).isEqualTo(EstadoEmergencia.CONVOCATORIA_ABIERTA);
        assertThatThrownBy(() -> e.exigirConvocatoriaAbierta(despues))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("venció");
    }

    @Test
    void rechazaOfertasSiLaConvocatoriaNoEstaAbierta() {
        Emergencia e = Fixtures.emergencia();

        assertThatThrownBy(() -> e.exigirConvocatoriaAbierta(T0)).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void cerrarEsIdempotente() {
        Emergencia e = Fixtures.emergenciaConConvocatoriaAbierta(2);
        e.cerrarConvocatoria();
        e.cerrarConvocatoria();

        assertThat(e.getEstado()).isEqualTo(EstadoEmergencia.CONVOCATORIA_CERRADA);
        assertThat(e.convocatoriaAbierta(T0)).isFalse();
    }

    @Test
    void reabrirVuelveALotesDefinidosConNuevaVentana() {
        Emergencia e = cerrada();

        e.registrarDecision(DecisionCcr.REABRIR, 12);
        e.abrirConvocatoria(T0.plus(Duration.ofHours(30)));

        assertThat(e.getEstado()).isEqualTo(EstadoEmergencia.CONVOCATORIA_ABIERTA);
        assertThat(e.getFechaCierreConvocatoria()).isEqualTo(T0.plus(Duration.ofHours(42)));
    }

    @Test
    void reformularHabilitaNuevamenteLaEdicionDeLotes() {
        Emergencia e = cerrada();

        e.registrarDecision(DecisionCcr.REFORMULAR, null);

        assertThat(e.getEstado()).isEqualTo(EstadoEmergencia.EN_REVISION);
        assertThat(e.admiteEdicionDeLotes()).isTrue();
    }

    @Test
    void continuarParcialPasaAValidacion() {
        Emergencia e = cerrada();

        e.registrarDecision(DecisionCcr.CONTINUAR_PARCIAL, null);

        assertThat(e.getEstado()).isEqualTo(EstadoEmergencia.EN_VALIDACION);
    }

    @ParameterizedTest
    @EnumSource(DecisionCcr.class)
    void noSePuedeDecidirConLaConvocatoriaAbierta(DecisionCcr decision) {
        Emergencia e = Fixtures.emergenciaConConvocatoriaAbierta(2);

        assertThatThrownBy(() -> e.registrarDecision(decision, null)).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void noSePuedeRevisarDosVeces() {
        Emergencia e = Fixtures.emergencia();
        e.revisar(null, null, T0);

        assertThatThrownBy(() -> e.revisar(null, null, T0)).isInstanceOf(ReglaNegocioException.class);
    }

    private static Emergencia cerrada() {
        Emergencia e = Fixtures.emergenciaConConvocatoriaAbierta(24);
        e.cerrarConvocatoria();
        return e;
    }
}
