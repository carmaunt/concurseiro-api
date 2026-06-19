package br.com.concurseiro.api.analytics.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnalyticsEventRequest(
        @NotBlank @Size(max = 80) String eventName,
        @Size(max = 160) String deviceId,
        @Size(max = 160) String sessionId,
        @Size(max = 120) String screenName,
        @Size(max = 120) String filterName,
        @Size(max = 40) String questionId,
        Boolean acertou,
        Long disciplinaId,
        @Size(max = 160) String disciplinaNome,
        Long assuntoId,
        @Size(max = 160) String assuntoNome,
        Long subassuntoId,
        @Size(max = 160) String subassuntoNome,
        @Min(0) @Max(86400) Integer interactionDurationSeconds,
        @Size(max = 40) String appVersion,
        @Size(max = 40) String platform,
        @Size(max = 4000) String metadataJson
) {}
