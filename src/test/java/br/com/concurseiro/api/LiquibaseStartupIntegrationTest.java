package br.com.concurseiro.api;

import br.com.concurseiro.api.analytics.repository.AnalyticsQueryRepository;
import br.com.concurseiro.api.analytics.repository.AnalyticsQueryRepository.AnalyticsFilter;
import br.com.concurseiro.api.analytics.service.AnalyticsInsightsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class LiquibaseStartupIntegrationTest {

    @Autowired JdbcTemplate jdbc;
    @Autowired AnalyticsQueryRepository analyticsQueries;
    @Autowired AnalyticsInsightsService analyticsInsights;
    @Autowired MockMvc mockMvc;

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
        org.junit.jupiter.api.Assertions.assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_name = 'categorias_editoriais'", Integer.class));
        org.junit.jupiter.api.Assertions.assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_name = 'tags_editoriais'", Integer.class));
        org.junit.jupiter.api.Assertions.assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_name = 'conteudos_portal_tags'", Integer.class));
    }

    @Test
    void taxonomiasAdminRecusamUsuarioNaoAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/admin/categorias-editoriais")).andExpect(status().isUnauthorized());
    }

    @Test
    void taxonomiasAdminRecusamUsuarioSemPermissao() throws Exception {
        mockMvc.perform(get("/api/v1/admin/categorias-editoriais")
                .with(user("estudante").authorities(() -> "ROLE_USUARIO_FINAL")))
                .andExpect(status().isForbidden());
    }

    @Test
    void taxonomiasAdminPermitemAdministrador() throws Exception {
        mockMvc.perform(get("/api/v1/admin/categorias-editoriais")
                .with(user("admin").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void conteudosPublicosFiltramPorCategoriaSlug() throws Exception {
        seedConteudosComTaxonomias();
        mockMvc.perform(get("/api/v1/conteudos").param("tipo", "NOTICIA").param("category", "seguranca-publica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[*].category.slug").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("seguranca-publica"))));
    }

    @Test
    void conteudosPublicosFiltramPorTagEConteudoComMultiplasTags() throws Exception {
        seedConteudosComTaxonomias();
        mockMvc.perform(get("/api/v1/conteudos").param("tipo", "NOTICIA").param("tag", "policia-federal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[?(@.slug == 'concurso-policia-federal')].tags.length()").value(2));
    }

    @Test
    void conteudosPublicosCombinamCategoriaTagEBusca() throws Exception {
        seedConteudosComTaxonomias();
        mockMvc.perform(get("/api/v1/conteudos")
                        .param("tipo", "NOTICIA")
                        .param("search", "polícia")
                        .param("category", "seguranca-publica")
                        .param("tag", "policia-federal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].slug").value("concurso-policia-federal"));
    }

    @Test
    void conteudosPublicosPreservamFiltrosNaPaginacaoReal() throws Exception {
        seedConteudosComTaxonomias();
        mockMvc.perform(get("/api/v1/conteudos")
                        .param("tipo", "NOTICIA").param("category", "seguranca-publica")
                        .param("page", "1").param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page.number").value(1))
                .andExpect(jsonPath("$.data.page.size").value(1))
                .andExpect(jsonPath("$.data.page.totalElements").value(3))
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void conteudosPublicosRetornamVazioParaSlugsInexistentesOuArquivados() throws Exception {
        seedConteudosComTaxonomias();
        mockMvc.perform(get("/api/v1/conteudos").param("tipo", "NOTICIA").param("category", "inexistente"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.page.totalElements").value(0));
        mockMvc.perform(get("/api/v1/conteudos").param("tipo", "NOTICIA").param("category", "categoria-arquivada"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.page.totalElements").value(0));
        mockMvc.perform(get("/api/v1/conteudos").param("tipo", "NOTICIA").param("tag", "tag-arquivada"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.page.totalElements").value(0));
    }

    @Test
    void conteudosPublicosExcluemRascunhosEFuturosMesmoComFiltro() throws Exception {
        seedConteudosComTaxonomias();
        mockMvc.perform(get("/api/v1/conteudos").param("tipo", "NOTICIA").param("category", "seguranca-publica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.slug == 'rascunho-seguranca')]").isEmpty())
                .andExpect(jsonPath("$.data.content[?(@.slug == 'noticia-futura')]").isEmpty());
    }

    @Test
    void opcoesPublicasRetornamSomenteAtivasComConteudoPublicadoDoTipo() throws Exception {
        seedConteudosComTaxonomias();
        mockMvc.perform(get("/api/v1/categorias/publicas").param("tipo", "NOTICIA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].slug", org.hamcrest.Matchers.containsInAnyOrder("seguranca-publica", "metodos-de-estudo")));
        mockMvc.perform(get("/api/v1/tags/publicas").param("tipo", "NOTICIA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].slug", org.hamcrest.Matchers.containsInAnyOrder("policia-federal", "edital")));
    }

    private void seedConteudosComTaxonomias() {
        jdbc.update("DELETE FROM conteudos_portal_tags");
        jdbc.update("DELETE FROM conteudos_portal");
        jdbc.update("DELETE FROM tags_editoriais");
        jdbc.update("DELETE FROM categorias_editoriais");
        jdbc.update("""
                INSERT INTO categorias_editoriais (nome, slug, status) VALUES
                  ('Segurança Pública', 'seguranca-publica', 'ATIVA'),
                  ('Métodos de estudo', 'metodos-de-estudo', 'ATIVA'),
                  ('Categoria arquivada', 'categoria-arquivada', 'ARQUIVADA')
                """);
        jdbc.update("""
                INSERT INTO tags_editoriais (nome, slug, status) VALUES
                  ('Polícia Federal', 'policia-federal', 'ATIVA'),
                  ('Edital', 'edital', 'ATIVA'),
                  ('Tag arquivada', 'tag-arquivada', 'ARQUIVADA')
                """);
        jdbc.update("""
                INSERT INTO conteudos_portal
                  (titulo, slug, resumo, conteudo, status, tipo, destaque, publicado_em, created_at, updated_at, categoria_id)
                VALUES
                  ('Concurso da Polícia Federal', 'concurso-policia-federal', 'Resumo polícia', 'Conteúdo polícia', 'PUBLICADO', 'NOTICIA', false, CURRENT_TIMESTAMP - interval '2 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM categorias_editoriais WHERE slug='seguranca-publica')),
                  ('Novo edital de segurança', 'edital-seguranca', 'Resumo edital', 'Conteúdo edital', 'PUBLICADO', 'NOTICIA', false, CURRENT_TIMESTAMP - interval '1 day', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM categorias_editoriais WHERE slug='seguranca-publica')),
                  ('Revisão para concursos', 'revisao-concursos', 'Resumo revisão', 'Conteúdo revisão', 'PUBLICADO', 'NOTICIA', false, CURRENT_TIMESTAMP - interval '3 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM categorias_editoriais WHERE slug='metodos-de-estudo')),
                  ('Rascunho segurança', 'rascunho-seguranca', 'Resumo', 'Conteúdo', 'RASCUNHO', 'NOTICIA', false, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM categorias_editoriais WHERE slug='seguranca-publica')),
                  ('Notícia futura', 'noticia-futura', 'Resumo', 'Conteúdo', 'PUBLICADO', 'NOTICIA', false, CURRENT_TIMESTAMP + interval '5 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM categorias_editoriais WHERE slug='seguranca-publica')),
                  ('Conteúdo antigo', 'conteudo-categoria-arquivada', 'Resumo', 'Conteúdo', 'PUBLICADO', 'NOTICIA', false, CURRENT_TIMESTAMP - interval '4 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM categorias_editoriais WHERE slug='categoria-arquivada')),
                  ('Segurança com tag antiga', 'seguranca-tag-arquivada', 'Resumo', 'Conteúdo', 'PUBLICADO', 'NOTICIA', false, CURRENT_TIMESTAMP - interval '5 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM categorias_editoriais WHERE slug='seguranca-publica'))
                """);
        jdbc.update("""
                INSERT INTO conteudos_portal_tags (conteudo_id, tag_id)
                SELECT c.id, t.id FROM conteudos_portal c CROSS JOIN tags_editoriais t
                WHERE (c.slug='concurso-policia-federal' AND t.slug IN ('policia-federal','edital'))
                   OR (c.slug='edital-seguranca' AND t.slug='edital')
                   OR (c.slug='revisao-concursos' AND t.slug='policia-federal')
                   OR (c.slug='conteudo-categoria-arquivada' AND t.slug='policia-federal')
                   OR (c.slug='seguranca-tag-arquivada' AND t.slug='tag-arquivada')
                """);
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
