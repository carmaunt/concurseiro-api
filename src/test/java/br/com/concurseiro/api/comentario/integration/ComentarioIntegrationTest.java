package br.com.concurseiro.api.comentario.integration;

import br.com.concurseiro.api.comentario.model.Comentario;
import br.com.concurseiro.api.comentario.repository.ComentarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ComentarioIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("jwt.secret", () -> "uma-chave-super-segura-com-32-chars");
        registry.add("jwt.expiration-ms", () -> "3600000");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @BeforeEach
    void limparBanco() {
        comentarioRepository.deleteAll();
    }

    @Test
    void devePersistirListarCurtirEDescurtirComentario() throws Exception {
        mockMvc.perform(post("/api/v1/questoes/123/comentarios")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "autor": "Mauricio",
                                  "texto": "Comentário de integração"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.questaoId").value("123"))
                .andExpect(jsonPath("$.data.autor").value("Mauricio"))
                .andExpect(jsonPath("$.data.texto").value("Comentário de integração"))
                .andExpect(jsonPath("$.data.curtidas").value(0))
                .andExpect(jsonPath("$.data.descurtidas").value(0));

        Comentario comentario = comentarioRepository
                .findByQuestaoId("123", PageRequest.of(0, 10))
                .getContent()
                .stream()
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/api/v1/questoes/123/comentarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(comentario.getId()))
                .andExpect(jsonPath("$.data.content[0].texto").value("Comentário de integração"));

        mockMvc.perform(post("/api/v1/comentarios/{id}/curtir", comentario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.curtidas").value(1))
                .andExpect(jsonPath("$.data.descurtidas").value(0));

        mockMvc.perform(post("/api/v1/comentarios/{id}/descurtir", comentario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.curtidas").value(1))
                .andExpect(jsonPath("$.data.descurtidas").value(1));
    }
}