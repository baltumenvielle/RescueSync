package ar.edu.unlp.dssd.rescuesync;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Reloj controlable desde los tests para ejercitar la ventana temporal. */
public class RelojTest extends Clock {

    private volatile Instant ahora;

    public RelojTest(Instant inicio) {
        this.ahora = inicio;
    }

    public void avanzar(Duration duracion) {
        ahora = ahora.plus(duracion);
    }

    public void fijar(Instant instante) {
        ahora = instante;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return ahora;
    }
}
