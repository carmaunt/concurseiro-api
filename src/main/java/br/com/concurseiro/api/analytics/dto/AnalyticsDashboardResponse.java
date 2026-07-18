package br.com.concurseiro.api.analytics.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record AnalyticsDashboardResponse(
        OffsetDateTime from,
        OffsetDateTime to,
        int onlineWindowMinutes,
        Overview overview,
        AcquisitionFunnel acquisitionFunnel,
        Activation activation,
        Engagement engagement,
        Retention retention,
        Content content,
        DataQuality dataQuality,
        List<DailyTrend> dailyTrend
) {
    public record Overview(long activeToday, long activePeriod, long realActive, long onlineNow,
                           long sessions, double averageSessionSeconds, long questionsToday,
                           long questionsPeriod, double averageAccuracy, long devices,
                           long identifiedUsers) {}
    public record AcquisitionFunnel(
            long portalVisitors,
            long storeClicks,
            long attributedInstalls,
            long activatedUsers,
            long eligibleForRetentionDay7,
            long retainedDay7,
            double portalToStoreRate,
            double storeToInstallRate,
            double installToActivationRate,
            double portalToActivationRate,
            double retentionDay7Rate,
            long totalInstallEvents,
            long linkedInstallEvents,
            double attributionCoverageRate,
            String status,
            String activationDefinition,
            String retentionDefinition
    ) {}
    public record Activation(long newIdentities, long appOpened, long firstQuestionViewed,
                             long firstQuestionAnswered, double activationRate,
                             double averageMinutesToFirstAnswer) {}
    public record Engagement(double questionsPerActive, double sessionsPerActive,
                             long identitiesWith10Questions, long identitiesWith50Questions,
                             long engagedLast7Days, long inactiveUsers) {}
    public record Retention(double day1, double day7, double day30, String method) {}
    public record Content(List<AnalyticsRankingItemResponse> topScreens,
                          List<AnalyticsRankingItemResponse> topFilters,
                          List<AnalyticsRankingItemResponse> disciplinesAccessed,
                          List<AnalyticsRankingItemResponse> disciplinesAnswered,
                          List<AnalyticsRankingItemResponse> subjectsAccessed,
                          List<AnalyticsRankingItemResponse> subjectsAnswered,
                          List<AnalyticsRankingItemResponse> subsubjectsAccessed,
                          List<AnalyticsRankingItemResponse> subsubjectsAnswered,
                          List<AnalyticsRankingItemResponse> mostWrongQuestions,
                          List<AnalyticsRankingItemResponse> mostCorrectQuestions,
                          long filtersWithoutResults, long searchesWithoutResults) {}
    public record DataQuality(long eventsLast24Hours, OffsetDateTime lastEventAt,
                              Map<String, Long> eventsByAppVersion, long unknownEvents,
                              double missingSessionPercent, double missingIdentityPercent,
                              List<AnalyticsRankingItemResponse> recentErrors) {}
    public record DailyTrend(LocalDate date, long active, long sessions, long questions,
                             double accuracy) {}
}
