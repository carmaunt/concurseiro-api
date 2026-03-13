package br.com.concurseiro.api.comentario.controller;

import br.com.concurseiro.api.comentario.model.Comentario;
import br.com.concurseiro.api.comentario.repository.ComentarioRepository;
import br.com.concurseiro.api.infra.security.JwtService;
import br.com.concurseiro.api.usuarios.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComentarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class ComentarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ComentarioRepository repository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private br.com.concurseiro.api.infra.security.RateLimitFilter rateLimitFilter;

    @Test
    void criar_deveRetornar201_quandoPayloadValido() throws Exception {
        Comentario salvo = new Comentario();
        ReflectionTestUtils.setField(salvo, "id", 1L);
        ReflectionTestUtils.setField(salvo, "criadoEm", OffsetDateTime.now());
        salvo.setQuestaoId("123");
        salvo.setAutor("Mauricio");
        salvo.setTexto("Comentário de teste");
        salvo.setCurtidas(0);
        salvo.setDescurtidas(0);

        when(repository.save(any(Comentario.class))).thenReturn(salvo);

        mockMvc.perform(post("/api/v1/questoes/123/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "autor": "Mauricio",
                                  "texto": "Comentário de teste"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.questaoId").value("123"))
                .andExpect(jsonPath("$.data.autor").value("Mauricio"))
                .andExpect(jsonPath("$.data.texto").value("Comentário de teste"));

        ArgumentCaptor<Comentario> captor = ArgumentCaptor.forClass(Comentario.class);
        verify(repository).save(captor.capture());
        assertEquals("123", captor.getValue().getQuestaoId());
        assertEquals("Mauricio", captor.getValue().getAutor());
        assertEquals("Comentário de teste", captor.getValue().getTexto());
    }

    @Test
    void listar_deveOrdenarPorCurtidas_quandoParametroOrdenarForCurtidas() throws Exception {
        Comentario comentario = new Comentario();
        ReflectionTestUtils.setField(comentario, "id", 3L);
        ReflectionTestUtils.setField(comentario, "criadoEm", OffsetDateTime.now());
        comentario.setQuestaoId("123");
        comentario.setAutor("Mauricio");
        comentario.setTexto("Comentário de teste");
        comentario.setCurtidas(5);
        comentario.setDescurtidas(1);

        Page<Comentario> page = new PageImpl<>(
                List.of(comentario),
                PageRequest.of(0, 20, Sort.by(DESC, "curtidas")),
                1
        );

        when(repository.findByQuestaoId(eq("123"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/questoes/123/comentarios")
                        .param("ordenar", "curtidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(3))
                .andExpect(jsonPath("$.data.content[0].curtidas").value(5));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByQuestaoId(eq("123"), captor.capture());
        Pageable pageable = captor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
        assertEquals(Sort.by(DESC, "curtidas"), pageable.getSort());
    }

    @Test
    void curtir_deveRetornar404_quandoComentarioNaoExiste() throws Exception {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/comentarios/999/curtir"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Comentário não encontrado"));
    }

    @Test
    void criar_deveRetornar400_quandoPayloadInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/questoes/123/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "autor": "",
                                  "texto": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Falha de validação"))
                .andExpect(jsonPath("$.fields.autor", containsString("must not be blank")))
                .andExpect(jsonPath("$.fields.texto", containsString("must not be blank")));
    }
}