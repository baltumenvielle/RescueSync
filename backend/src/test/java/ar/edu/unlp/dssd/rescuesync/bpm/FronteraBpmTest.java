package ar.edu.unlp.dssd.rescuesync.bpm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Regla de arquitectura: solo el paquete integration.bonita puede depender de la integración con Bonita. */
class FronteraBpmTest {

    private static final Path RAIZ = Path.of("src/main/java/ar/edu/unlp/dssd/rescuesync");

    @Test
    void ningunaClaseFueraDeIntegrationBonitaLaImporta() throws IOException {
        try (Stream<Path> archivos = Files.walk(RAIZ)) {
            List<Path> infractores = archivos
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !RAIZ.relativize(p).startsWith(Path.of("integration", "bonita")))
                    .filter(FronteraBpmTest::referenciaIntegracionBonita)
                    .toList();
            assertThat(infractores).isEmpty();
        }
    }

    private static boolean referenciaIntegracionBonita(Path archivo) {
        try {
            return Files.readString(archivo).contains("rescuesync.integration.bonita");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
