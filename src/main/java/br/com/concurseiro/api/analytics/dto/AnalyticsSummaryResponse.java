package br.com.concurseiro.api.analytics.dto;

public record AnalyticsSummaryResponse(
        long totalDevices,
        long activeUsersToday,
        long activeUsersInPeriod,
        long onlineNow,
        long questionsAnsweredToday,
        long questionsAnsweredInPeriod,
        double averageInteractionSeconds
) {}
