package br.com.concurseiro.api.infra.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class AdminKeyValidator {

    @Value("${app.admin.api-key:}")
    private String adminApiKey;

    private final Environment environment;

    public AdminKeyValidator(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validate() {

        boolean isLocal = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("local"));

        if (!isLocal) {

            if (adminApiKey == null || adminApiKey.isBlank()) {
                throw new IllegalStateException(
                        "APP_ADMIN_API_KEY não configurada. A aplicação não pode iniciar."
                );
            }

            if (adminApiKey.length() < 32) {
                throw new IllegalStateException(
                        "APP_ADMIN_API_KEY fraca. Use pelo menos 32 caracteres."
                );
            }
        }
    }
}