package br.com.concurseiro.api.analytics.service;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class AnalyticsPeriodResolverTest {
    private final AnalyticsPeriodResolver resolver = new AnalyticsPeriodResolver();

    @Test void seteDiasComparaIntervaloImediatamenteAnteriorDeMesmaDuracao() {
        var value = resolver.resolve("7d", null, null);
        assertEquals(value.current().from(), value.previous().to());
        assertEquals(Duration.between(value.current().from(), value.current().to()), Duration.between(value.previous().from(), value.previous().to()));
    }
    @Test void hojeComparaOntemAteMesmoHorario() {
        var value = resolver.resolve("today", null, null);
        assertEquals(value.current().from().minusDays(1), value.previous().from());
        assertEquals(value.current().to().minusDays(1), value.previous().to());
    }
    @Test void personalizadoAceitaHojeSemCriarDataFutura() {
        var value = resolver.resolve("custom", LocalDate.now().minusDays(2), LocalDate.now());
        assertTrue(value.current().to().isBefore(java.time.OffsetDateTime.now(AnalyticsPeriodResolver.APP_ZONE).plusSeconds(1)));
    }
}
