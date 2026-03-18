package br.com.concurseiro.api.config;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PrometheusEndpointConfig {

    @Bean
    public PrometheusScrapeEndpoint prometheusEndpoint(PrometheusRegistry prometheusRegistry) {
        System.out.println(">>> PrometheusEndpointConfig carregado");
        return new PrometheusScrapeEndpoint(prometheusRegistry, null);
    }
}