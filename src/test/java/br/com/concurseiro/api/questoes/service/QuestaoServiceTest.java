package br.com.concurseiro.api.questoes.service;

import br.com.concurseiro.api.catalogo.assunto.model.Assunto;
import br.com.concurseiro.api.catalogo.assunto.repository.AssuntoRepository;
import br.com.concurseiro.api.catalogo.banca.model.Banca;
import br.com.concurseiro.api.catalogo.banca.repository.BancaRepository;
import br.com.concurseiro.api.catalogo.disciplina.model.Disciplina;
import br.com.concurseiro.api.catalogo.disciplina.repository.DisciplinaRepository;
import br.com.concurseiro.api.catalogo.instituicao.model.Instituicao;
import br.com.concurseiro.api.catalogo.instituicao.repository.InstituicaoRepository;
import br.com.concurseiro.api.catalogo.subassunto.model.SubAssunto;
import br.com.concurseiro.api.catalogo.subassunto.repository.SubAssuntoRepository;
import br.com.concurseiro.api.questoes.dto.QuestaoRequest;
import br.com.concurseiro.api.questoes.enunciado.model.Enunciado;
import br.com.concurseiro.api.questoes.enunciado.service.EnunciadoService;
import br.com.concurseiro.api.questoes.model.Questao;
import br.com.concurseiro.api.questoes.repository.QuestaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QuestaoServiceTest {

    private QuestaoService service;

    private QuestaoRepository questaoRepository;
    private DisciplinaRepository disciplinaRepository;
    private AssuntoRepository assuntoRepository;
    private SubAssuntoRepository subAssuntoRepository;
    private BancaRepository bancaRepository;
    private InstituicaoRepository instituicaoRepository;
    private EnunciadoService enunciadoService;

    @BeforeEach
    void setup() {
        questaoRepository = mock(QuestaoRepository.class);
        disciplinaRepository = mock(DisciplinaRepository.class);
        assuntoRepository = mock(AssuntoRepository.class);
        subAssuntoRepository = mock(SubAssuntoRepository.class);
        bancaRepository = mock(BancaRepository.class);
        instituicaoRepository = mock(InstituicaoRepository.class);
        enunciadoService = mock(EnunciadoService.class);

        when(enunciadoService.resolverEnunciado(any(), any())).thenAnswer(invocation -> {
            Enunciado enunciado = new Enunciado();
            enunciado.setId(invocation.getArgument(0));
            enunciado.setConteudo(invocation.getArgument(1));
            enunciado.setHashSha256("hash-teste");
            return enunciado;
        });

        service = new QuestaoService(
                questaoRepository,
                disciplinaRepository,
                assuntoRepository,
                subAssuntoRepository,
                bancaRepository,
                instituicaoRepository,
                enunciadoService,
                null
        );
    }

    private Disciplina mockDisciplina(Long id, String nome) {
            Disciplina disciplina = mock(Disciplina.class);
            when(disciplina.getId()).thenReturn(id);
            when(disciplina.getNome()).thenReturn(nome);
            when(disciplinaRepository.findById(id)).thenReturn(Optional.of(disciplina));
            return disciplina;
    }

    private Assunto mockAssunto(Long id, String nome) {
        Assunto assunto = mock(Assunto.class);
        when(assunto.getId()).thenReturn(id);
        when(assunto.getNome()).thenReturn(nome);
        when(assuntoRepository.findById(id)).thenReturn(Optional.of(assunto));
        return assunto;
    }

    private SubAssunto mockSubAssunto(Long id, String nome, Assunto assunto) {
        SubAssunto subAssunto = mock(SubAssunto.class);
        when(subAssunto.getId()).thenReturn(id);
        when(subAssunto.getNome()).thenReturn(nome);
        when(subAssunto.getAssunto()).thenReturn(assunto);
        when(subAssuntoRepository.findById(id)).thenReturn(Optional.of(subAssunto));
        return subAssunto;
    }

    private Banca mockBanca(Long id, String nome) {
        Banca banca = mock(Banca.class);
        when(banca.getId()).thenReturn(id);
        when(banca.getNome()).thenReturn(nome);
        when(bancaRepository.findById(id)).thenReturn(Optional.of(banca));
        return banca;
    }

    private Instituicao mockInstituicao(Long id, String nome) {
            Instituicao instituicao = mock(Instituicao.class);
            when(instituicao.getId()).thenReturn(id);
            when(instituicao.getNome()).thenReturn(nome);
            when(instituicaoRepository.findById(id)).thenReturn(Optional.of(instituicao));
            return instituicao;
    }

    private QuestaoRequest requestPadrao(
            Long disciplinaId,
            Long assuntoId,
            Long bancaId,
            Long instituicaoId,
            String alternativas,
            String modalidade,
            String gabarito
    ) {
        return new QuestaoRequest(
                "Enunciado teste",
                "Texto da questão",
                alternativas,
                disciplinaId,
                assuntoId,
                bancaId,
                instituicaoId,
                2024,
                "Agente",
                "Médio",
                modalidade,
                gabarito
        );
    }

    @Test
    void listarFiltradoPaginado_deveFalhar_quandoSortForInvalido() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.listarFiltradoPaginado(
                        null,
                        1L,
                        2L,
                        null,
                        3L,
                        4L,
                        null,
                        null,
                        null,
                        null,
                        0,
                        10,
                        "hackerField,desc"
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("sort inválido"));
    }

    @Test
    void cadastrar_deveSalvarComNormalizacaoEIdGerado() {
        mockDisciplina(1L, "Direito Constitucional");
        mockAssunto(2L, "Direitos Fundamentais");
        mockBanca(3L, "CEBRASPE");
        mockInstituicao(4L, "PC-BA");

        when(questaoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuestaoRequest req = requestPadrao(
                1L,
                2L,
                3L,
                4L,
                "A) A\nB) B\nC) C\nD) D\nE) E",
                "CERTO E ERRADO",
                "CERTO"
        );

        Questao result = service.cadastrar(req);

        assertNotNull(result.getIdQuestion());
        assertTrue(result.getIdQuestion().startsWith("Q"));
        assertEquals("CERTO_ERRADO", result.getModalidade());
        assertEquals("C", result.getGabarito());
        assertEquals("Direito Constitucional", result.getDisciplina());
        assertEquals("Direitos Fundamentais", result.getAssunto());
        assertEquals("CEBRASPE", result.getBanca());
        assertEquals("PC-BA", result.getInstituicao());

        verify(enunciadoService).resolverEnunciado(null, "Enunciado teste");
        verify(questaoRepository, times(1)).save(any());
    }

    @Test
    void cadastrar_deveReutilizarEnunciadoSelecionadoPorId() {
        mockDisciplina(1L, "Direito Constitucional");
        mockAssunto(2L, "Direitos Fundamentais");
        mockBanca(3L, "CEBRASPE");
        mockInstituicao(4L, "PC-BA");

        Enunciado existente = new Enunciado();
        existente.setId(15L);
        existente.setConteudo("Julgue o item subsequente.");
        existente.setHashSha256("hash-existente");
        when(enunciadoService.resolverEnunciado(15L, null)).thenReturn(existente);
        when(questaoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuestaoRequest req = new QuestaoRequest(
                null,
                "Texto da questão",
                "C) Certo\nE) Errado",
                null,
                null,
                null,
                null,
                null,
                15L,
                1L,
                2L,
                null,
                3L,
                4L,
                2024,
                "Agente",
                "Superior",
                "CERTO_ERRADO",
                "C"
        );

        Questao result = service.cadastrar(req);

        assertSame(existente, result.getEnunciadoCatalogo());
        assertEquals("Julgue o item subsequente.", result.getEnunciado());
        verify(enunciadoService).resolverEnunciado(15L, null);
    }

    @Test
    void cadastrar_deveSalvarSubAssunto_quandoInformadoECompativelComAssunto() {
        mockDisciplina(1L, "Português");
        Assunto assunto = mockAssunto(2L, "Interpretação e compreensão de texto");
        SubAssunto subAssunto = mockSubAssunto(5L, "Inferência", assunto);
        mockBanca(3L, "CEBRASPE");
        mockInstituicao(4L, "Polícia Federal");

        when(questaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuestaoRequest req = new QuestaoRequest(
                "Julgue o item.",
                "O texto permite inferir determinada conclusão.",
                "C) Certo\nE) Errado",
                null,
                null,
                null,
                null,
                null,
                1L,
                2L,
                5L,
                3L,
                4L,
                2025,
                "Agente",
                "Superior",
                "CERTO_ERRADO",
                "C"
        );

        Questao result = service.cadastrar(req);

        assertEquals(subAssunto, result.getSubAssuntoCatalogo());
        assertEquals("Inferência", result.getSubAssunto());
        verify(questaoRepository).save(any());
    }

    @Test
    void cadastrar_deveFalhar_quandoSubAssuntoNaoPertenceAoAssunto() {
        mockDisciplina(1L, "Português");
        Assunto assunto = mockAssunto(2L, "Interpretação e compreensão de texto");
        Assunto outroAssunto = mock(Assunto.class);
        when(outroAssunto.getId()).thenReturn(99L);
        mockSubAssunto(5L, "Inferência", outroAssunto);
        mockBanca(3L, "CEBRASPE");
        mockInstituicao(4L, "Polícia Federal");

        QuestaoRequest req = new QuestaoRequest(
                "Julgue o item.",
                "O texto permite inferir determinada conclusão.",
                "C) Certo\nE) Errado",
                null,
                null,
                null,
                null,
                null,
                1L,
                assunto.getId(),
                5L,
                3L,
                4L,
                2025,
                "Agente",
                "Superior",
                "CERTO_ERRADO",
                "C"
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.cadastrar(req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Subassunto não pertence"));
        verify(questaoRepository, never()).save(any());
    }

    @Test
    void cadastrar_deveFalhar_quandoInstituicaoIdNaoExiste() {
        mockDisciplina(1L, "Direito Constitucional");
        mockAssunto(2L, "Direitos Fundamentais");
        mockBanca(3L, "CEBRASPE");
        when(instituicaoRepository.findById(999L)).thenReturn(Optional.empty());

        QuestaoRequest req = requestPadrao(
                1L,
                2L,
                3L,
                999L,
                "A) A\nB) B\nC) C\nD) D\nE) E",
                "A_E",
                "C"
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.cadastrar(req));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Instituição não encontrada"));
    }

    @Test
    void cadastrar_deveFalhar_quandoDisciplinaIdNaoExiste() {
        when(disciplinaRepository.findById(999L)).thenReturn(Optional.empty());

        QuestaoRequest req = requestPadrao(
                999L,
                2L,
                3L,
                4L,
                "A) A\nB) B\nC) C\nD) D\nE) E",
                "A_E",
                "C"
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.cadastrar(req));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Disciplina não encontrada"));
    }

    @Test
    void cadastrar_deveNormalizarParaAD_quandoMultEscolhaSemAlternativaE() {
        mockDisciplina(1L, "Direito Constitucional");
        mockAssunto(2L, "Direitos Fundamentais");
        mockBanca(3L, "CEBRASPE");
        mockInstituicao(4L, "PC-BA");

        when(questaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuestaoRequest req = requestPadrao(
                1L,
                2L,
                3L,
                4L,
                "A) A\nB) B\nC) C\nD) D",
                "MÚLTIPLA ESCOLHA",
                "C"
        );

        Questao salvo = service.cadastrar(req);

        assertEquals("A_D", salvo.getModalidade());
        assertEquals("C", salvo.getGabarito());
        verify(questaoRepository).save(any());
    }

    @Test
    void cadastrar_deveNormalizarParaAE_quandoMultEscolhaComAlternativaE() {
        mockDisciplina(1L, "Direito Constitucional");
        mockAssunto(2L, "Direitos Fundamentais");
        mockBanca(3L, "CEBRASPE");
        mockInstituicao(4L, "PC-BA");

        when(questaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuestaoRequest req = requestPadrao(
                1L,
                2L,
                3L,
                4L,
                "A) A\nB) B\nC) C\nD) D\nE) E",
                "MÚLTIPLA ESCOLHA",
                "E"
        );

        Questao salvo = service.cadastrar(req);

        assertEquals("A_E", salvo.getModalidade());
        assertEquals("E", salvo.getGabarito());
        verify(questaoRepository).save(any());
    }

    @Test
    void cadastrar_deveAceitarGabaritoAnulada_emQualquerModalidade() {
        mockDisciplina(1L, "Direito Constitucional");
        mockAssunto(2L, "Direitos Fundamentais");
        mockBanca(3L, "CEBRASPE");
        mockInstituicao(4L, "PC-BA");

        when(questaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuestaoRequest req = requestPadrao(
                1L,
                2L,
                3L,
                4L,
                "A) A\nB) B\nC) C\nD) D",
                "A_D",
                "ANULADA"
        );

        Questao salvo = service.cadastrar(req);

        assertEquals("A_D", salvo.getModalidade());
        assertEquals("X", salvo.getGabarito());
        verify(questaoRepository).save(any());
    }

    @Test
    void cadastrar_deveFalhar_quandoModalidadeAD_eGabaritoForE() {
        mockDisciplina(1L, "Direito Constitucional");
        mockAssunto(2L, "Direitos Fundamentais");
        mockBanca(3L, "CEBRASPE");
        mockInstituicao(4L, "PC-BA");

        QuestaoRequest req = requestPadrao(
                1L,
                2L,
                3L,
                4L,
                "A) A\nB) B\nC) C\nD) D",
                "MÚLTIPLA ESCOLHA",
                "E"
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.cadastrar(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("gabarito incompatível"));
        verify(questaoRepository, never()).save(any());
    }

    @Test
    void cadastrar_deveFalhar_quandoCertoErrado_comGabaritoInvalido() {
        mockDisciplina(1L, "Direito Constitucional");
        mockAssunto(2L, "Direitos Fundamentais");
        mockBanca(3L, "CEBRASPE");
        mockInstituicao(4L, "PC-BA");

        QuestaoRequest req = requestPadrao(
                1L,
                2L,
                3L,
                4L,
                "A) A\nB) B",
                "CERTO E ERRADO",
                "A"
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.cadastrar(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("gabarito incompatível"));
        verify(questaoRepository, never()).save(any());
    }

    @Test
    void cadastrar_deveFalhar_quandoModalidadeForDesconhecida() {
        mockDisciplina(1L, "Direito Constitucional");
        mockAssunto(2L, "Direitos Fundamentais");
        mockBanca(3L, "CEBRASPE");
        mockInstituicao(4L, "PC-BA");

        QuestaoRequest req = requestPadrao(
                1L,
                2L,
                3L,
                4L,
                "A) A\nB) B\nC) C\nD) D",
                "DISCURSIVA",
                "C"
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.cadastrar(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Modalidade inválida"));
        verify(questaoRepository, never()).save(any());
    }

    @Test
    void buscarPorIdQuestion_deveRetornarQuestao_quandoExiste() {
        Questao q = new Questao();
        q.setIdQuestion("Q123456789012345");
        when(questaoRepository.findDetalhadaByIdQuestion("Q123456789012345")).thenReturn(Optional.of(q));

        Questao result = service.buscarPorIdQuestion("Q123456789012345");
        assertEquals("Q123456789012345", result.getIdQuestion());
    }

    @Test
    void buscarPorIdQuestion_deveFalhar_quandoNaoExiste() {
        when(questaoRepository.findDetalhadaByIdQuestion("QNOTFOUND")).thenReturn(Optional.empty());

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

        mockDisciplina(1L, "Direito Penal");
        mockAssunto(2L, "Crimes");
        mockBanca(3L, "CEBRASPE");
        mockInstituicao(4L, "PC-BA");

        when(questaoRepository.findByIdQuestion("Q123456789012345")).thenReturn(Optional.of(existente));
        when(questaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuestaoRequest req = new QuestaoRequest(
                "Enunciado atualizado",
                "Texto atualizado",
                "A) A\nB) B\nC) C\nD) D\nE) E",
                1L,
                2L,
                3L,
                4L,
                2025,
                "Delegado",
                "Superior",
                "MÚLTIPLA ESCOLHA",
                "B"
        );

        Questao result = service.atualizar("Q123456789012345", req);
        assertEquals("Enunciado atualizado", result.getEnunciado());
        assertEquals("A_E", result.getModalidade());
        assertEquals("B", result.getGabarito());
        assertEquals("Direito Penal", result.getDisciplina());
        verify(questaoRepository).save(any());
    }

    @Test
    void atualizar_deveFalhar_quandoNaoExiste() {
        when(questaoRepository.findByIdQuestion("QNOTFOUND")).thenReturn(Optional.empty());

        QuestaoRequest req = new QuestaoRequest(
                "Enunciado",
                "Texto",
                "A) A\nB) B\nC) C\nD) D",
                1L,
                2L,
                3L,
                4L,
                2024,
                "Cargo",
                "Nivel",
                "A_D",
                "A"
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizar("QNOTFOUND", req));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
