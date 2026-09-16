package ar.edu.unlp.dssd.rescuesync.bpm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NombresTareasTest {

    @Test
    void comparaIgnorandoTildesMayusculasYEspacios() {
        assertThat(NombresTareas.coincide("Revisión de Información", "revision de  informacion ")).isTrue();
        assertThat(NombresTareas.coincide("Desglose en lotes", "Desglose de lotes")).isFalse();
        assertThat(NombresTareas.coincide(null, "x")).isFalse();
    }
}
