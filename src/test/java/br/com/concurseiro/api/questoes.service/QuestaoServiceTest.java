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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class QuestaoServiceTest {

    private QuestaoService service;

    @BeforeEach
    void setup() {
        QuestaoRepository questaoRepository = mock(QuestaoRepository.class);
        DisciplinaRepository disciplinaRepository = mock(DisciplinaRepository.class);
        AssuntoRepository assuntoRepository = mock(AssuntoRepository.class);
        BancaRepository bancaRepository = mock(BancaRepository.class);
        InstituicaoRepository instituicaoRepository = mock(InstituicaoRepository.class);

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
}