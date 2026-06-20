package br.com.concurseiro.api.analytics.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;

@Component
public class AnalyticsPeriodResolver {
    static final ZoneId APP_ZONE = ZoneId.of("America/Sao_Paulo");

    public Comparison resolve(String period, LocalDate startDate, LocalDate endDate) {
        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);
        String value = period == null ? "7d" : period;
        OffsetDateTime from;
        OffsetDateTime to = now.plusNanos(1);
        OffsetDateTime previousFrom;
        OffsetDateTime previousTo;
        String label;

        switch (value) {
            case "today" -> {
                from = now.toLocalDate().atStartOfDay(APP_ZONE).toOffsetDateTime();
                previousFrom = from.minusDays(1); previousTo = to.minusDays(1); label = "Hoje";
            }
            case "7d" -> { from = now.toLocalDate().minusDays(6).atStartOfDay(APP_ZONE).toOffsetDateTime(); label = "Últimos 7 dias"; previousTo = from; previousFrom = previousTo.minus(Duration.between(from, to)); }
            case "30d" -> { from = now.toLocalDate().minusDays(29).atStartOfDay(APP_ZONE).toOffsetDateTime(); label = "Últimos 30 dias"; previousTo = from; previousFrom = previousTo.minus(Duration.between(from, to)); }
            case "current_month" -> {
                from = now.toLocalDate().withDayOfMonth(1).atStartOfDay(APP_ZONE).toOffsetDateTime(); label = "Mês atual";
                YearMonth previousMonth = YearMonth.from(now).minusMonths(1);
                previousFrom = previousMonth.atDay(1).atStartOfDay(APP_ZONE).toOffsetDateTime();
                int day = Math.min(now.getDayOfMonth(), previousMonth.lengthOfMonth());
                previousTo = previousMonth.atDay(day).atTime(now.toLocalTime()).atZone(APP_ZONE).toOffsetDateTime().plusNanos(1);
            }
            case "custom" -> {
                if (startDate == null || endDate == null || endDate.isBefore(startDate)) throw bad("startDate e endDate válidos são obrigatórios");
                from = startDate.atStartOfDay(APP_ZONE).toOffsetDateTime();
                OffsetDateTime requestedTo = endDate.plusDays(1).atStartOfDay(APP_ZONE).toOffsetDateTime();
                to = requestedTo.isAfter(now) ? now.plusNanos(1) : requestedTo;
                if (!from.isBefore(to)) throw bad("período personalizado não pode estar no futuro");
                label = "Período personalizado"; previousTo = from; previousFrom = previousTo.minus(Duration.between(from, to));
            }
            default -> throw bad("period deve ser today, 7d, 30d, current_month ou custom");
        }
        if (Duration.between(from, to).compareTo(Duration.ofDays(366)) > 0) throw bad("o período máximo é de 366 dias");
        return new Comparison(label, new Range(from, to), new Range(previousFrom, previousTo));
    }

    private ResponseStatusException bad(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    public record Range(OffsetDateTime from, OffsetDateTime to) {}
    public record Comparison(String label, Range current, Range previous) {}
}
