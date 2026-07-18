package br.com.concurseiro.api.questoes.controller;

import br.com.concurseiro.api.infra.config.SecurityConfig;
import br.com.concurseiro.api.infra.security.AuthCookieService;
import br.com.concurseiro.api.infra.security.JwtAuthFilter;
import br.com.concurseiro.api.infra.security.JwtService;
import br.com.concurseiro.api.infra.security.RateLimitFilter;
import br.com.concurseiro.api.questoes.dto.RespostaQuestaoAmostraResponse;
import br.com.concurseiro.api.questoes.resposta.service.RespostaQuestaoUsuarioService;
import br.com.concurseiro.api.questoes.service.QuestaoService;
import br.com.concurseiro.api.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestaoController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, RateLimitFilter.class})
@ImportAutoConfiguration({
        SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
@TestPropertySource(properties = "cors.allowed-origins=http://localhost:3000")
class QuestaoAmostraSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestaoService service;

    @MockitoBean
    private RespostaQuestaoUsuarioService respostaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private AuthCookieService authCookieService;

    @Test
    void amostraDevePermitirListagemECorrecaoAnonimas() throws Exception {
        when(service.listarAmostra(5)).thenReturn(List.of());
        when(service.responderAmostra(any(), any())).thenReturn(
                new RespostaQuestaoAmostraResponse("QAMOSTRA1", "A", "A", true, null)
        );

        mockMvc.perform(get("/api/v1/questoes/amostra"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/questoes/amostra/QAMOSTRA1/respostas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"respostaSelecionada":"A"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void areaWebContinuaExigindoAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/questoes/web"))
                .andExpect(status().isUnauthorized());
    }
}
