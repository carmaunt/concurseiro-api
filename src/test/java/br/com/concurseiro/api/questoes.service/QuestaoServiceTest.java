package br.com.concurseiro.api.questoes.service;

import br.com.concurseiro.api.catalogo.assunto.repository.AssuntoRepository;
import br.com.concurseiro.api.catalogo.banca.repository.BancaRepository;
import br.com.concurseiro.api.catalogo.disciplina.repository.DisciplinaRepository;
import br.com.concurseiro.api.catalogo.instituicao.repository.InstituicaoRepository;
import br.com.concurseiro.api.questoes.dto.QuestaoRequest;
import br.com.concurseiro.api.questoes.repository.QuestaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuestaoServiceTest {

    private QuestaoService service;

    private QuestaoRepository questaoRepository;
    private DisciplinaRepository disciplinaRepository;
    private AssuntoRepository assuntoRepository;
    private BancaRepository bancaRepository;
    private InstituicaoRepository instituicaoRepository;

    @BeforeEach
    void setup() {
                questaoRepository = mock(QuestaoRepository.class);
        disciplinaRepository = mock(DisciplinaRepository.class);
        assuntoRepository = mock(AssuntoRepository.class);
        bancaRepository = mock(BancaRepository.class);
        instituicaoRepository = mock(InstituicaoRepository.class);
service = new QuestaoService(
                questaoRepository,
                disciplinaRepository,
                assuntoRepository,
                bancaRepository,
                instituicaoRepository
        );
    }

    @Test
    void cadastrar_deveFalhar_quandoInstituicaoIdForNulo() {
        QuestaoRequest req = new QuestaoRequest(
                "Enunciado teste",
                "Texto da questão",
                "A) A\nB) B\nC) C\nD) D\nE) E",
                "Direito Constitucional",
                "Direitos Fundamentais",
                "CEBRASPE",
                "PC-BA",
                null,
                null,
                null,
                null,
                2024,
                "Agente",
                "Médio",
                "Múltipla escolha",
                "C"
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.cadastrar(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("instituicaoId é obrigatório"));
    }

    @Test
    void listarFiltradoPaginado_deveFalhar_quandoSortForInvalido() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.listarFiltradoPaginado(
                        null,
                        null, null,
                        null, null,
                        null, null,
                        null, 1L,
                        null,
                        null, null, null,
                        0, 10,
                        "hackerField,desc" // inválido
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("sort inválido"));
    }

    @Test
    void cadastrar_deveSalvarComNormalizacaoEIdGerado() {

        // Arrange
        var disciplinaRepo = mock(DisciplinaRepository.class);
        var assuntoRepo = mock(AssuntoRepository.class);
        var bancaRepo = mock(BancaRepository.class);
        var instituicaoRepo = mock(InstituicaoRepository.class);
        var questaoRepo = mock(QuestaoRepository.class);

        service = new QuestaoService(
                questaoRepo,
                disciplinaRepo,
                assuntoRepo,
                bancaRepo,
                instituicaoRepo
        );

        var instituicao = new br.com.concurseiro.api.catalogo.instituicao.model.Instituicao();
        
        instituicao.setNome("PC-BA");

        when(instituicaoRepo.findById(1L)).thenReturn(java.util.Optional.of(instituicao));
        when(questaoRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuestaoRequest req = new QuestaoRequest(
                "Enunciado teste",
                "Texto da questão",
                "A) A\nB) B\nC) C\nD) D\nE) E",
                "Direito Constitucional",
                "Direitos Fundamentais",
                "CEBRASPE",
                "PC-BA",
                null,
                null,
                null,
                1L,
                2024,
                "Agente",
                "Médio",
                "CERTO E ERRADO",
                "CERTO"
        );

        // Act
        var result = service.cadastrar(req);

        // Assert
        assertNotNull(result.getIdQuestion());
        assertTrue(result.getIdQuestion().startsWith("Q"));
        assertEquals("CERTO_ERRADO", result.getModalidade());
        assertEquals("C", result.getGabarito());

        verify(questaoRepo, times(1)).save(any());
    }
    @Test
    void cadastrar_deveFalhar_quandoInstituicaoIdNaoExiste() {
        // arrange
        when(instituicaoRepository.findById(999L)).thenReturn(Optional.empty());

        QuestaoRequest req = new QuestaoRequest(
                "Enunciado teste",
                "Texto da questão",
                "A) A\nB) B\nC) C\nD) D\nE) E",
                "Direito Constitucional",
                "Direitos Fundamentais",
                "CEBRASPE",
                "PC-BA",
                null,
                null,
                null,
                999L,   // instituicaoId não existe
                2024,
                "Agente",
                "Médio",
                "Múltipla escolha",
                "C"
        );

        // act + assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.cadastrar(req));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Instituição não encontrada"));
    }

    @Test
    void cadastrar_deveNormalizarParaAD_quandoMultEscolhaSemAlternativaE() {

        // arrange
        var inst = new br.com.concurseiro.api.catalogo.instituicao.model.Instituicao();
        inst.setNome("PC-BA");

        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(inst));
        when(questaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuestaoRequest req = new QuestaoRequest(
                "Enunciado teste",
                "Texto da questão",
                "A) A\nB) B\nC) C\nD) D", // sem alternativa E
                "Direito Constitucional",
                "Direitos Fundamentais",
                "CEBRASPE",
                "PC-BA",
                null,
                null,
                null,
                1L,
                2024,
                "Agente",
                "Médio",
                "MÚLTIPLA ESCOLHA",
                "C"
        );

        // act
        var salvo = service.cadastrar(req);

        // assert
        assertEquals("A_D", salvo.getModalidade());
        assertEquals("C", salvo.getGabarito());
        verify(questaoRepository).save(any());
    }

}