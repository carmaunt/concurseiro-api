package br.com.concurseiro.api.analytics.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record AnalyticsDashboardResponse(
        OffsetDateTime from,
        OffsetDateTime to,
        AnalyticsSummaryResponse summary,
        List<AnalyticsRankingItemResponse> topScreens,
        List<AnalyticsRankingItemResponse> topFilters,
        List<AnalyticsRankingItemResponse> topDisciplinas,
        List<AnalyticsRankingItemResponse> topAssuntos,
        List<AnalyticsRankingItemResponse> topSubassuntos
) {}
