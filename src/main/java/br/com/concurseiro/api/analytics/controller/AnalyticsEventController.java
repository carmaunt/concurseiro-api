package br.com.concurseiro.api.analytics.controller;

import br.com.concurseiro.api.analytics.dto.AnalyticsEventRequest;
import br.com.concurseiro.api.analytics.dto.AnalyticsEventResponse;
import br.com.concurseiro.api.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Analytics", description = "Coleta de eventos de uso do aplicativo")
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsEventController {

    private final AnalyticsService analyticsService;

    public AnalyticsEventController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Operation(summary = "Registrar evento de uso do app")
    @SecurityRequirements
    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public AnalyticsEventResponse registrarEvento(
            @RequestBody @Valid AnalyticsEventRequest request,
            Authentication authentication
    ) {
        return analyticsService.registrarEvento(request, authentication);
    }
}
