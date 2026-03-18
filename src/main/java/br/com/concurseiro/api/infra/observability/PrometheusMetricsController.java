package br.com.concurseiro.api.infra.observability;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PrometheusMetricsController {

    private final PrometheusMeterRegistry prometheusMeterRegistry;

    public PrometheusMetricsController(PrometheusMeterRegistry prometheusMeterRegistry) {
        this.prometheusMeterRegistry = prometheusMeterRegistry;
    }

    @GetMapping(value = "/internal/prometheus", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> scrape() {
        return ResponseEntity.ok(prometheusMeterRegistry.scrape());
    }
}