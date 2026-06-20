package br.com.concurseiro.api.analytics.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record AnalyticsInsightsResponse(
        PeriodInfo period, PeriodInfo previousPeriod, Status status, int score,
        Integer previousScore, Double scoreChangePercent, Confidence confidence,
        String confidenceReason, String title, String summary,
        boolean possibleAutomatedTraffic, String automatedTrafficReason,
        Map<String, MetricInsight> metrics, List<Driver> drivers,
        List<Recommendation> recommendations
) {
    public enum Status { GOOD, BAD, STABLE, INSUFFICIENT_DATA }
    public enum Confidence { HIGH, MEDIUM, LOW }
    public enum Trend { UP, DOWN, STABLE, NO_BASE }
    public record PeriodInfo(String label, OffsetDateTime startDate, OffsetDateTime endDate) {}
    public record MetricInsight(double current, double previous, Double changePercent,
                                Trend trend, String interpretation) {}
    public record Driver(String type, String title, String description, String metric,
                         Object currentValue, Object previousValue, Double changePercent,
                         String severity) {}
    public record Recommendation(String priority, String title, String description,
                                 String relatedMetric) {}
}
