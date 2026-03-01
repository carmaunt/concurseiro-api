package br.com.concurseiro.api.questoes.service;

import br.com.concurseiro.api.catalogo.assunto.repository.AssuntoRepository;
import br.com.concurseiro.api.catalogo.banca.repository.BancaRepository;
import br.com.concurseiro.api.catalogo.disciplina.repository.DisciplinaRepository;
import br.com.concurseiro.api.catalogo.instituicao.repository.InstituicaoRepository;
import br.com.concurseiro.api.questoes.dto.QuestaoRequest;
import br.com.concurseiro.api.questoes.model.Questao;
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
    @Test
    void cadastrar_deveNormalizarParaAE_quandoMultEscolhaComAlternativaE() {
        // arrange
        var inst = new br.com.concurseiro.api.catalogo.instituicao.model.Instituicao();
        inst.setNome("PC-BA");

        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(inst));
        when(questaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuestaoRequest req = new QuestaoRequest(
                "Enunciado teste",
                "Texto da questão",
                "A) A\nB) B\nC) C\nD) D\nE) E", // com E)
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
                "E" // em A_E, E é válido
        );

        // act
        var salvo = service.cadastrar(req);

        // assert
        assertEquals("A_E", salvo.getModalidade());
        assertEquals("E", salvo.getGabarito());
        verify(questaoRepository).save(any());
    }

    @Test
    void cadastrar_deveFalhar_quandoModalidadeAD_eGabaritoForE() {
        // arrange (instituição existe pra não cair em NOT_FOUND)
        var inst = new br.com.concurseiro.api.catalogo.instituicao.model.Instituicao();
        inst.setNome("PC-BA");
        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(inst));

        QuestaoRequest req = new QuestaoRequest(
                "Enunciado teste",
                "Texto da questão",
                "A) A\nB) B\nC) C\nD) D", // sem E) => normaliza para A_D
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
                "E" // inválido pra A_D
        );

        // act + assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.cadastrar(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("gabarito incompatível"));
        verify(questaoRepository, never()).save(any());
    }

    @Test
    void cadastrar_deveFalhar_quandoCertoErrado_comGabaritoInvalido() {
        // arrange
        var inst = new br.com.concurseiro.api.catalogo.instituicao.model.Instituicao();
        inst.setNome("PC-BA");
        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(inst));

        QuestaoRequest req = new QuestaoRequest(
                "Enunciado teste",
                "Texto da questão",
                "A) A\nB) B", // alternativas não importam aqui
                "Direito Constitucional",
                "Direitos Fundamentais",
                "CEBRASPE",
                "PC-BA",
                null,
                null,
                null,
                1L,                 // instituicaoId OK
                2024,
                "Agente",
                "Médio",
                "CERTO E ERRADO",   // vai normalizar pra CERTO_ERRADO
                "A"                 // inválido para CERTO_ERRADO
        );

        // act + assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.cadastrar(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("gabarito incompatível"));
        verify(questaoRepository, never()).save(any());
    }

    @Test
    void cadastrar_deveFalhar_quandoModalidadeForDesconhecida() {
        // arrange
        var inst = new br.com.concurseiro.api.catalogo.instituicao.model.Instituicao();
        inst.setNome("PC-BA");
        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(inst));

        QuestaoRequest req = new QuestaoRequest(
                "Enunciado teste",
                "Texto da questão",
                "A) A\nB) B\nC) C\nD) D",
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
                "DISCURSIVA", // modalidade inválida (fora do domínio)
                "C"
        );

        // act + assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.cadastrar(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Modalidade inválida"));

        // garante que não tentou persistir nada
        verify(questaoRepository, never()).save(any());
    }

    @Test
    void buscarPorIdQuestion_deveRetornarQuestao_quandoExiste() {
        Questao q = new Questao();
        q.setIdQuestion("Q123456789012345");
        when(questaoRepository.findByIdQuestion("Q123456789012345")).thenReturn(Optional.of(q));

        Questao result = service.buscarPorIdQuestion("Q123456789012345");
        assertEquals("Q123456789012345", result.getIdQuestion());
    }

    @Test
    void buscarPorIdQuestion_deveFalhar_quandoNaoExiste() {
        when(questaoRepository.findByIdQuestion("QNOTFOUND")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.buscarPorIdQuestion("QNOTFOUND"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void excluir_deveDeletar_quandoExiste() {
        Questao q = new Questao();
        q.setIdQuestion("Q123456789012345");
        when(questaoRepository.findByIdQuestion("Q123456789012345")).thenReturn(Optional.of(q));

        service.excluir("Q123456789012345");
        verify(questaoRepository).delete(q);
    }

    @Test
    void excluir_deveFalhar_quandoNaoExiste() {
        when(questaoRepository.findByIdQuestion("QNOTFOUND")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.excluir("QNOTFOUND"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(questaoRepository, never()).delete(any(Questao.class));
    }

    @Test
    void atualizar_deveAtualizarQuestao_quandoExiste() {
        Questao existente = new Questao();
        existente.setIdQuestion("Q123456789012345");

        var inst = new br.com.concurseiro.api.catalogo.instituicao.model.Instituicao();
        inst.setNome("PC-BA");

        when(questaoRepository.findByIdQuestion("Q123456789012345")).thenReturn(Optional.of(existente));
        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(inst));
        when(questaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuestaoRequest req = new QuestaoRequest(
                "Enunciado atualizado",
                "Texto atualizado",
                "A) A\nB) B\nC) C\nD) D\nE) E",
                "Direito Penal",
                "Crimes",
                "CEBRASPE",
                "PC-BA",
                null, null, null, 1L,
                2025, "Delegado", "Superior",
                "MÚLTIPLA ESCOLHA", "B"
        );

        Questao result = service.atualizar("Q123456789012345", req);
        assertEquals("Enunciado atualizado", result.getEnunciado());
        assertEquals("A_E", result.getModalidade());
        assertEquals("B", result.getGabarito());
        verify(questaoRepository).save(any());
    }

    @Test
    void atualizar_deveFalhar_quandoNaoExiste() {
        when(questaoRepository.findByIdQuestion("QNOTFOUND")).thenReturn(Optional.empty());

        QuestaoRequest req = new QuestaoRequest(
                "Enunciado", "Texto", "A) A\nB) B\nC) C\nD) D",
                "Disc", "Assunto", "Banca", "Inst",
                null, null, null, 1L,
                2024, "Cargo", "Nivel",
                "A_D", "A"
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizar("QNOTFOUND", req));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

}
