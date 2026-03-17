package br.com.concurseiro.api.prova.service;

import br.com.concurseiro.api.catalogo.assunto.repository.AssuntoRepository;
import br.com.concurseiro.api.catalogo.banca.repository.BancaRepository;
import br.com.concurseiro.api.catalogo.disciplina.repository.DisciplinaRepository;
import br.com.concurseiro.api.catalogo.instituicao.model.Instituicao;
import br.com.concurseiro.api.catalogo.instituicao.repository.InstituicaoRepository;
import br.com.concurseiro.api.prova.dto.ProvaRequest;
import br.com.concurseiro.api.prova.dto.ProvaResponse;
import br.com.concurseiro.api.prova.model.Prova;
import br.com.concurseiro.api.prova.repository.ProvaRepository;
import br.com.concurseiro.api.questoes.repository.QuestaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProvaServiceTest {

    @Mock
    private ProvaRepository provaRepository;

    @Mock
    private QuestaoRepository questaoRepository;

    @Mock
    private InstituicaoRepository instituicaoRepository;

    @Mock
    private DisciplinaRepository disciplinaRepository;

    @Mock
    private AssuntoRepository assuntoRepository;

    @Mock
    private BancaRepository bancaRepository;

    private ProvaService service;

    @BeforeEach
        void setUp() {
                service = new ProvaService(
                        provaRepository,
                        questaoRepository,
                        instituicaoRepository,
                        disciplinaRepository,
                        assuntoRepository,
                        bancaRepository
                );
        }

    @Test
    void deveCriarProvaComSucesso() {
        Instituicao inst = novaInstituicao(1L, "PC-BA");

        ProvaRequest request = new ProvaRequest(
                " FCC ",
                1L,
                2023,
                " Analista Judiciário ",
                " SUPERIOR ",
                "A_E"
        );

        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(inst));
        when(provaRepository.existsByBancaIgnoreCaseAndInstituicaoCatalogoIdAndAnoAndCargoIgnoreCaseAndNivelIgnoreCaseAndModalidadeIgnoreCase(
                "FCC", 1L, 2023, "Analista Judiciário", "SUPERIOR", "A_E"
        )).thenReturn(false);

        when(provaRepository.save(any(Prova.class))).thenAnswer(invocation -> {
            Prova prova = invocation.getArgument(0);
            setId(prova, 10L);
            return prova;
        });

        ProvaResponse response = service.criarProva(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("FCC", response.banca());
        assertEquals("PC-BA", response.instituicao());
        assertEquals(1L, response.instituicaoId());
        assertEquals(2023, response.ano());
        assertEquals("Analista Judiciário", response.cargo());
        assertEquals("SUPERIOR", response.nivel());
        assertEquals("A_E", response.modalidade());
        assertEquals(0L, response.totalQuestoes());

        verify(provaRepository).existsByBancaIgnoreCaseAndInstituicaoCatalogoIdAndAnoAndCargoIgnoreCaseAndNivelIgnoreCaseAndModalidadeIgnoreCase(
                "FCC", 1L, 2023, "Analista Judiciário", "SUPERIOR", "A_E"
        );
        verify(provaRepository).save(any(Prova.class));
    }

    @Test
    void deveBloquearDuplicidadeAntesDoSave() {
        Instituicao inst = novaInstituicao(1L, "PC-BA");

        ProvaRequest request = new ProvaRequest(
                "TESTE",
                1L,
                2030,
                "Cargo Teste",
                "SUPERIOR",
                "A_E"
        );

        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(inst));
        when(provaRepository.existsByBancaIgnoreCaseAndInstituicaoCatalogoIdAndAnoAndCargoIgnoreCaseAndNivelIgnoreCaseAndModalidadeIgnoreCase(
                "TESTE", 1L, 2030, "Cargo Teste", "SUPERIOR", "A_E"
        )).thenReturn(true);

        ErrorResponseException ex = assertThrows(
                ErrorResponseException.class,
                () -> service.criarProva(request)
        );

        assertEquals(HttpStatus.CONFLICT.value(), ex.getStatusCode().value());

        ProblemDetail pd = ex.getBody();
        assertNotNull(pd);
        assertEquals("Prova duplicada", pd.getTitle());
        assertEquals("Já existe uma prova cadastrada com esse cabeçalho", pd.getDetail());
        assertEquals("https://concurseiro.dev/errors/prova-duplicada", pd.getType().toString());

        verify(provaRepository, never()).save(any(Prova.class));
    }

    @Test
    void deveRetornarErroAmigavelQuandoBancoDispararConstraintDeDuplicidade() {
        Instituicao inst = novaInstituicao(1L, "PC-BA");

        ProvaRequest request = new ProvaRequest(
                "TESTE",
                1L,
                2030,
                "Cargo Teste",
                "SUPERIOR",
                "A_E"
        );

        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(inst));
        when(provaRepository.existsByBancaIgnoreCaseAndInstituicaoCatalogoIdAndAnoAndCargoIgnoreCaseAndNivelIgnoreCaseAndModalidadeIgnoreCase(
                "TESTE", 1L, 2030, "Cargo Teste", "SUPERIOR", "A_E"
        )).thenReturn(false);

        when(provaRepository.save(any(Prova.class)))
                .thenThrow(new DataIntegrityViolationException("violates unique constraint uk_provas_cabecalho"));

        ErrorResponseException ex = assertThrows(
                ErrorResponseException.class,
                () -> service.criarProva(request)
        );

        assertEquals(HttpStatus.CONFLICT.value(), ex.getStatusCode().value());

        ProblemDetail pd = ex.getBody();
        assertNotNull(pd);
        assertEquals("Prova duplicada", pd.getTitle());
        assertEquals("Já existe uma prova cadastrada com esse cabeçalho", pd.getDetail());
        assertEquals("https://concurseiro.dev/errors/prova-duplicada", pd.getType().toString());
    }

    private Instituicao novaInstituicao(Long id, String nome) {
        Instituicao inst = new Instituicao();
        setId(inst, id);
        inst.setNome(nome);
        return inst;
    }

    private void setId(Object target, Long id) {
        try {
            var field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (Exception e) {
            throw new RuntimeException("Não foi possível atribuir id via reflexão no teste", e);
        }
    }
}