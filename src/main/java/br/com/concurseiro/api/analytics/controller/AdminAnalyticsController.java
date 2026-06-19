package br.com.concurseiro.api.analytics.controller;

import br.com.concurseiro.api.analytics.dto.AnalyticsDashboardResponse;
import br.com.concurseiro.api.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@Tag(name = "Admin Analytics", description = "Métricas agregadas, sem exposição de dados pessoais")
@RestController
@RequestMapping("/api/v1/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    public AdminAnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Operation(summary = "Consultar dashboard de analytics")
    @GetMapping("/dashboard")
    public AnalyticsDashboardResponse dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) Long disciplinaId,
            @RequestParam(required = false) Long assuntoId,
            @RequestParam(required = false) Long subassuntoId
    ) {
        return analyticsService.dashboard(from, to, disciplinaId, assuntoId, subassuntoId);
    }
}
