package br.com.concurseiro.api.prova.controller;

import br.com.concurseiro.api.infra.security.JwtService;
import br.com.concurseiro.api.prova.dto.ProvaResponse;
import br.com.concurseiro.api.prova.service.ProvaService;
import br.com.concurseiro.api.questoes.model.Questao;
import br.com.concurseiro.api.usuarios.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.ErrorResponseException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.HttpStatus;

@WebMvcTest(ProvaController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProvaControllerTest {

        @org.springframework.test.context.bean.override.mockito.MockitoBean
        private br.com.concurseiro.api.infra.security.LoginRateLimitService loginRateLimitService;

        @org.junit.jupiter.api.BeforeEach
        void setUpRateLimit() {
        org.mockito.Mockito.when(
                loginRateLimitService.isAllowed(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString()
                )
        ).thenReturn(true);}

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProvaService service;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void criar_deveRetornar201_quandoPayloadValido() throws Exception {
        ProvaResponse response = new ProvaResponse(
                1L,
                "CESPE",
                "PC-BA",
                1L,
                2024,
                "Analista",
                "SUPERIOR",
                "CERTO_ERRADO",
                0L,
                OffsetDateTime.now()
        );

        when(service.criarProva(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/provas")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "banca": "CESPE",
                                  "instituicaoId": 1,
                                  "ano": 2024,
                                  "cargo": "Analista",
                                  "nivel": "SUPERIOR",
                                  "modalidade": "CERTO_ERRADO"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.banca").value("CESPE"))
                .andExpect(jsonPath("$.data.totalQuestoes").value(0));
    }

    @Test
    void buscar_deveRetornar200_quandoProvaExiste() throws Exception {
        ProvaResponse response = new ProvaResponse(
                1L,
                "CESPE",
                "PC-BA",
                1L,
                2024,
                "Analista",
                "SUPERIOR",
                "CERTO_ERRADO",
                1L,
                OffsetDateTime.now()
        );

        when(service.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/provas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.totalQuestoes").value(1));
    }

    @Test
    void listar_deveRetornarPaginaDeProvas() throws Exception {
        ProvaResponse prova1 = new ProvaResponse(5L, "FCC", "PC-BA", 1L, 2023, "Cargo 3", "SUPERIOR", "A_E", 0L, OffsetDateTime.now());
        ProvaResponse prova2 = new ProvaResponse(4L, "FCC", "PC-BA", 1L, 2023, "Cargo 2", "SUPERIOR", "A_E", 0L, OffsetDateTime.now());

        when(service.listar(0, 2)).thenReturn(new PageImpl<>(List.of(prova1, prova2), PageRequest.of(0, 2), 5));

        mockMvc.perform(get("/api/v1/provas?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(5))
                .andExpect(jsonPath("$.data.content[1].id").value(4))
                .andExpect(jsonPath("$.data.page.totalElements").value(5))
                .andExpect(jsonPath("$.data.page.totalPages").value(3));
    }

        @Test
        void lancarQuestao_deveRetornar201_quandoPayloadValido() throws Exception {
        Questao questao = new Questao();
        questao.setIdQuestion("QC123");
        questao.setProvaId(1L);
        questao.setEnunciado("Texto de enunciado");
        questao.setQuestao("O princípio da legalidade está previsto na Constituição.");
        questao.setAlternativas("");
        questao.setAno(2024);
        questao.setCargo("Analista");
        questao.setNivel("SUPERIOR");
        questao.setModalidade("CERTO_ERRADO");
        questao.setGabarito("C");
        ReflectionTestUtils.setField(questao, "criadoEm", OffsetDateTime.now());

        when(service.cadastrarQuestao(eq(1L), org.mockito.ArgumentMatchers.any())).thenReturn(questao);

        mockMvc.perform(post("/api/v1/provas/1/questoes")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                "enunciado": "Texto de enunciado",
                                "questao": "O princípio da legalidade está previsto na Constituição.",
                                "alternativas": "",
                                "disciplinaId": 1,
                                "assuntoId": 1,
                                "gabarito": "C"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.idQuestion").value("QC123"))
                .andExpect(jsonPath("$.data.provaId").value(1))
                .andExpect(jsonPath("$.data.gabarito").value("C"));

        ArgumentCaptor<Long> provaIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(service).cadastrarQuestao(provaIdCaptor.capture(), org.mockito.ArgumentMatchers.any());
        assertEquals(1L, provaIdCaptor.getValue());
        }

        @Test
        void criar_deveRetornar409_quandoProvaDuplicada() throws Exception {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Prova duplicada");
        pd.setType(URI.create("https://concurseiro.dev/errors/prova-duplicada"));
        pd.setDetail("Já existe uma prova cadastrada com esse cabeçalho");

        when(service.criarProva(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new ErrorResponseException(HttpStatus.CONFLICT, pd, null));

        mockMvc.perform(post("/api/v1/provas")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                "banca": "TESTE",
                                "instituicaoId": 1,
                                "ano": 2030,
                                "cargo": "Cargo Teste",
                                "nivel": "SUPERIOR",
                                "modalidade": "A_E"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Prova duplicada"))
                .andExpect(jsonPath("$.detail").value("Já existe uma prova cadastrada com esse cabeçalho"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.type").value("https://concurseiro.dev/errors/prova-duplicada"))
                .andExpect(jsonPath("$.instance").value("/api/v1/provas"));
        }

    @Test
    void criar_deveRetornar400_quandoPayloadInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/provas")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "banca": "",
                                  "instituicaoId": null,
                                  "ano": 1800,
                                  "cargo": "",
                                  "nivel": "",
                                  "modalidade": "INVALIDA"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Falha de validação"))
                .andExpect(jsonPath("$.fields.banca", containsString("must not be blank")))
                .andExpect(jsonPath("$.fields.instituicaoId", containsString("must not be null")))
                .andExpect(jsonPath("$.fields.modalidade", containsString("modalidade deve ser")));
    }
}