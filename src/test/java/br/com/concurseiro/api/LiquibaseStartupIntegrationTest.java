package br.com.concurseiro.api;

import br.com.concurseiro.api.analytics.repository.AnalyticsQueryRepository;
import br.com.concurseiro.api.analytics.repository.AnalyticsQueryRepository.AnalyticsFilter;
import br.com.concurseiro.api.analytics.service.AnalyticsInsightsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class LiquibaseStartupIntegrationTest {

    @Autowired JdbcTemplate jdbc;
    @Autowired AnalyticsQueryRepository analyticsQueries;
    @Autowired AnalyticsInsightsService analyticsInsights;

    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("concurseiro_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.yaml");

        registry.add("jwt.secret", () -> "test-secret-123456789-test-secret-123456789");
        registry.add("app.admin.api-key", () -> "test-admin-key");
        registry.add("cors.allowed-origins", () -> "*");
        registry.add("firebase.enabled", () -> "false");
    }

    @BeforeEach
    void cleanAnalyticsEvents() {
        jdbc.update("DELETE FROM app_events");
    }

    @Test
    void contextLoads() {
    }

    @Test
    void analyticsQueriesAggregateRealPostgresData() {
        jdbc.update("""
                INSERT INTO app_events (device_id, event_name, screen_name, interaction_duration_ms, metadata)
                VALUES ('device-a', 'screen_view', 'home', 2000, '{}'::jsonb)
                """);
        jdbc.update("""
                INSERT INTO app_events (device_id, event_name, filter_name, interaction_duration_ms, metadata)
                VALUES ('device-a', 'filter_applied', 'disciplina', 3000, '{}'::jsonb)
                """);
        jdbc.update("""
                INSERT INTO app_events (device_id, event_name, interaction_duration_ms, metadata)
                VALUES ('device-b', 'question_answered', 4000, '{}'::jsonb)
                """);

        var from = java.time.OffsetDateTime.now().minusMinutes(1);
        var to = java.time.OffsetDateTime.now().plusMinutes(1);
        var filter = new AnalyticsFilter(from, to, null, null, null);

        org.junit.jupiter.api.Assertions.assertEquals(2, analyticsQueries.countDevices());
        org.junit.jupiter.api.Assertions.assertEquals(2, analyticsQueries.countActive(filter));
        org.junit.jupiter.api.Assertions.assertEquals(1, analyticsQueries.countEvent("question_answered", filter));
        org.junit.jupiter.api.Assertions.assertEquals(3.0, analyticsQueries.averageInteractionSeconds(filter), 0.001);
        org.junit.jupiter.api.Assertions.assertEquals("home", analyticsQueries.topScreens(filter, 10).getFirst().label());
        org.junit.jupiter.api.Assertions.assertEquals("disciplina", analyticsQueries.topFilters(filter, 10).getFirst().label());
    }

    @Test
    void analyticsInsightsRunsAllQueriesAgainstPostgres() {
        jdbc.update("""
                INSERT INTO app_events (anonymous_id, device_id, session_id, event_name, screen_name, app_version, metadata)
                VALUES ('integration-user', 'integration-device', 'integration-session', 'app_opened', 'home', '2.0.0', '{}'::jsonb),
                       ('integration-user', 'integration-device', 'integration-session', 'question_answered', 'questao', '2.0.0', '{"correct":true}'::jsonb)
                """);

        var result = analyticsInsights.insights("7d", null, null, null, null, null, null, null, null);

        org.junit.jupiter.api.Assertions.assertNotNull(result.status());
        org.junit.jupiter.api.Assertions.assertTrue(result.score() >= 0 && result.score() <= 100);
        org.junit.jupiter.api.Assertions.assertNotNull(result.previousPeriod());
        org.junit.jupiter.api.Assertions.assertFalse(result.drivers().isEmpty());
    }
}
