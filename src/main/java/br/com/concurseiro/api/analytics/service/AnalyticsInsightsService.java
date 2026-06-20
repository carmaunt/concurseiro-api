package br.com.concurseiro.api.analytics.service;

import br.com.concurseiro.api.analytics.dto.AnalyticsInsightsResponse;
import br.com.concurseiro.api.analytics.model.AnalyticsEventName;
import br.com.concurseiro.api.analytics.repository.AnalyticsQueryRepository;
import br.com.concurseiro.api.analytics.repository.AnalyticsQueryRepository.AnalyticsFilter;
import br.com.concurseiro.api.analytics.service.AnalyticsHealthAnalyzer.Snapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AnalyticsInsightsService {
    private final AnalyticsPeriodResolver periods;
    private final AnalyticsQueryRepository queries;
    private final AnalyticsHealthAnalyzer analyzer;

    public AnalyticsInsightsService(AnalyticsPeriodResolver periods, AnalyticsQueryRepository queries, AnalyticsHealthAnalyzer analyzer) {
        this.periods = periods; this.queries = queries; this.analyzer = analyzer;
    }

    @Transactional(readOnly = true)
    public AnalyticsInsightsResponse insights(String period, LocalDate startDate, LocalDate endDate,
            Long disciplinaId, Long assuntoId, Long subassuntoId, Long bancaId, Long instituicaoId, Long provaId) {
        var comparison = periods.resolve(period, startDate, endDate);
        AnalyticsFilter currentFilter = filter(comparison.current(), disciplinaId, assuntoId, subassuntoId, bancaId, instituicaoId, provaId);
        AnalyticsFilter previousFilter = filter(comparison.previous(), disciplinaId, assuntoId, subassuntoId, bancaId, instituicaoId, provaId);
        Snapshot current = snapshot(currentFilter);
        Snapshot previous = snapshot(previousFilter);
        var analysis = analyzer.analyze(current, previous);
        return new AnalyticsInsightsResponse(
                new AnalyticsInsightsResponse.PeriodInfo(comparison.label(), comparison.current().from(), comparison.current().to()),
                new AnalyticsInsightsResponse.PeriodInfo("Período anterior", comparison.previous().from(), comparison.previous().to()),
                analysis.status(), analysis.score(), analysis.previousScore(), analysis.scoreChangePercent(),
                analysis.confidence(), analysis.confidenceReason(), analysis.title(), analysis.summary(),
                analysis.possibleAutomatedTraffic(), analysis.automatedTrafficReason(), analysis.metrics(),
                analysis.drivers(), analysis.recommendations());
    }

    private Snapshot snapshot(AnalyticsFilter f) {
        long events = queries.countEvent(null, f);
        long screenViews = queries.topScreens(f, 100).stream().mapToLong(item -> item.total()).sum();
        return new Snapshot(events, queries.countActive(f), queries.countRealActive(f),
                queries.countActiveEvent("app_opened", f), queries.countActiveEvent("question_viewed", f),
                queries.countActiveEvent("question_answered", f), queries.countSessions(f),
                queries.countEvent("question_answered", f), queries.averageSessionSeconds(f),
                queries.retention(1, f), queries.retention(7, f), queries.retention(30, f),
                queries.countEvent("filter_applied", f), queries.countMetadataBoolean("filter_applied", "has_results", f, false),
                queries.countEvent("search_performed", f), queries.countMetadataBoolean("search_performed", "has_results", f, false),
                queries.countUnknown(f, AnalyticsEventName.OFFICIAL), queries.missingPercent(f, "e.session_id IS NULL"),
                queries.missingPercent(f, "e.user_id IS NULL AND NULLIF(e.anonymous_id,'') IS NULL AND NULLIF(e.device_id,'') IS NULL"),
                queries.countEvent("error_occurred", f), queries.inactiveUsers(f.to()), screenViews);
    }

    private AnalyticsFilter filter(AnalyticsPeriodResolver.Range range, Long disciplinaId, Long assuntoId, Long subassuntoId,
            Long bancaId, Long instituicaoId, Long provaId) {
        return new AnalyticsFilter(range.from(), range.to(), disciplinaId, assuntoId, subassuntoId, bancaId, instituicaoId, provaId);
    }
}
