package br.com.concurseiro.api.questoes.controller;

import br.com.concurseiro.api.infra.security.AuthCookieService;
import br.com.concurseiro.api.infra.security.JwtService;
import br.com.concurseiro.api.infra.security.LoginRateLimitService;
import br.com.concurseiro.api.questoes.dto.QuestaoWebResponse;
import br.com.concurseiro.api.questoes.dto.RespostaQuestaoAmostraResponse;
import br.com.concurseiro.api.questoes.resposta.service.RespostaQuestaoUsuarioService;
import br.com.concurseiro.api.questoes.service.QuestaoService;
import br.com.concurseiro.api.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestaoController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuestaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestaoService service;

    @MockitoBean
    private RespostaQuestaoUsuarioService respostaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthCookieService authCookieService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private LoginRateLimitService loginRateLimitService;

    @Test
    void listarAmostra_deveRetornarContratoSemGabarito() throws Exception {
        QuestaoWebResponse questao = new QuestaoWebResponse(
                "QAMOSTRA1", null, null, "Texto da questão", "A) Um\nB) Dois",
                null, null, null, null, null, null, null,
                "Português", 1L, "Interpretação", 2L, null, null,
                "CEBRASPE", 3L, "Órgão", 4L, 2026, "Analista", "Superior", "A_D",
                null, null
        );
        when(service.listarAmostra(5)).thenReturn(List.of(questao));

        mockMvc.perform(get("/api/v1/questoes/amostra"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].idQuestion").value("QAMOSTRA1"))
                .andExpect(jsonPath("$.data[0].gabarito").doesNotExist())
                .andExpect(jsonPath("$.data[0].explicacao").doesNotExist());
    }

    @Test
    void responderAmostra_deveCorrigirSemAutenticacaoNoContrato() throws Exception {
        when(service.responderAmostra(any(), any())).thenReturn(
                new RespostaQuestaoAmostraResponse(
                        "QAMOSTRA1", "A", "A", true, "Explicação"
                )
        );

        mockMvc.perform(post("/api/v1/questoes/amostra/QAMOSTRA1/respostas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"respostaSelecionada":"A"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.acertou").value(true))
                .andExpect(jsonPath("$.data.gabarito").value("A"));

        verify(service).responderAmostra(any(), any());
    }

    @Test
    void listarAmostra_deveRejeitarTamanhoAcimaDoLimite() throws Exception {
        mockMvc.perform(get("/api/v1/questoes/amostra").param("size", "11"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("size da amostra deve estar entre 1 e 10"));
    }
}
