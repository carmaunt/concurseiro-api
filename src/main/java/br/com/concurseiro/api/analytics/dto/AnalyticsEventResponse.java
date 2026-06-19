package br.com.concurseiro.api.analytics.dto;

import java.time.OffsetDateTime;

public record AnalyticsEventResponse(Long id, OffsetDateTime receivedAt) {}
