package br.com.concurseiro.api.analytics.controller;

import br.com.concurseiro.api.analytics.dto.AnalyticsDashboardResponse;
import br.com.concurseiro.api.analytics.dto.AnalyticsInsightsResponse;
import br.com.concurseiro.api.analytics.service.AnalyticsService;
import br.com.concurseiro.api.analytics.service.AnalyticsInsightsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Admin Analytics", description = "Métricas agregadas, sem exposição de dados pessoais")
@RestController
@RequestMapping("/api/v1/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;
    private final AnalyticsInsightsService analyticsInsightsService;

    public AdminAnalyticsController(AnalyticsService analyticsService, AnalyticsInsightsService analyticsInsightsService) {
        this.analyticsService = analyticsService;
        this.analyticsInsightsService = analyticsInsightsService;
    }

    @Operation(summary = "Consultar dashboard de analytics")
    @GetMapping("/dashboard")
    public AnalyticsDashboardResponse dashboard(
            @RequestParam(defaultValue = "7d") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long disciplinaId,
            @RequestParam(required = false) Long assuntoId,
            @RequestParam(required = false) Long subassuntoId,
            @RequestParam(required = false) Long bancaId,
            @RequestParam(required = false) Long instituicaoId,
            @RequestParam(required = false) Long provaId
    ) {
        return analyticsService.dashboard(period, startDate, endDate, disciplinaId, assuntoId, subassuntoId, bancaId, instituicaoId, provaId);
    }

    @Operation(summary = "Consultar diagnóstico automático de analytics")
    @GetMapping("/insights")
    public AnalyticsInsightsResponse insights(
            @RequestParam(defaultValue = "7d") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long disciplinaId,
            @RequestParam(required = false) Long assuntoId,
            @RequestParam(required = false) Long subassuntoId,
            @RequestParam(required = false) Long bancaId,
            @RequestParam(required = false) Long instituicaoId,
            @RequestParam(required = false) Long provaId
    ) {
        return analyticsInsightsService.insights(period, startDate, endDate, disciplinaId, assuntoId, subassuntoId, bancaId, instituicaoId, provaId);
    }
}
