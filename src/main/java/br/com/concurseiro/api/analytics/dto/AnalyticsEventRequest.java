package br.com.concurseiro.api.analytics.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record AnalyticsEventRequest(
        @NotBlank
        @Size(max = 80)
        @Pattern(regexp = "[a-z][a-z0-9_]*", message = "deve usar snake_case minúsculo")
        String eventName,
        @Size(max = 160) String deviceId,
        @Size(max = 160) String sessionId,
        @Size(max = 120) String screenName,
        @Size(max = 120) String filterName,
        @Size(max = 40) String questionId,
        Boolean answerCorrect,
        @Min(1) Long disciplinaId,
        @Min(1) Long assuntoId,
        @Min(1) Long subassuntoId,
        @Min(0) @Max(86_400_000) Long interactionDurationMs,
        @Size(max = 40) String appVersion,
        @Size(max = 40) String platform,
        Map<String, Object> metadata
) {}
