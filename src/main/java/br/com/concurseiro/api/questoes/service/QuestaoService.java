package br.com.concurseiro.api.questoes.service;

import br.com.concurseiro.api.catalogo.assunto.model.Assunto;
import br.com.concurseiro.api.catalogo.assunto.repository.AssuntoRepository;
import br.com.concurseiro.api.catalogo.banca.model.Banca;
import br.com.concurseiro.api.catalogo.banca.repository.BancaRepository;
import br.com.concurseiro.api.catalogo.disciplina.model.Disciplina;
import br.com.concurseiro.api.catalogo.disciplina.repository.DisciplinaRepository;
import br.com.concurseiro.api.catalogo.instituicao.model.Instituicao;
import br.com.concurseiro.api.catalogo.instituicao.repository.InstituicaoRepository;
import br.com.concurseiro.api.questoes.dto.QuestaoRequest;
import br.com.concurseiro.api.questoes.dto.QuestaoResponse;
import br.com.concurseiro.api.questoes.model.Questao;
import br.com.concurseiro.api.questoes.repository.QuestaoRepository;
import br.com.concurseiro.api.questoes.spec.QuestaoSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
public class QuestaoService {
    private static final Set<String> SORT_FIELDS_ALLOWED = Set.of(
            "ano",
            "criadoEm"
    );

    private final QuestaoRepository repository;
    private final DisciplinaRepository disciplinaRepository;
    private final AssuntoRepository assuntoRepository;
    private final BancaRepository bancaRepository;
    private final InstituicaoRepository instituicaoRepository;

    public QuestaoService(
            QuestaoRepository repository,
            DisciplinaRepository disciplinaRepository,
            AssuntoRepository assuntoRepository,
            BancaRepository bancaRepository,
            InstituicaoRepository instituicaoRepository
    ) {
        this.repository = repository;
        this.disciplinaRepository = disciplinaRepository;
        this.assuntoRepository = assuntoRepository;
        this.bancaRepository = bancaRepository;
        this.instituicaoRepository = instituicaoRepository;     
    }

    @Transactional
    public Questao cadastrar(QuestaoRequest request) {
        String modalidadeBruta = request.modalidade().trim();
        String gabaritoBruto = request.gabarito().trim().toUpperCase();

        String modalidade = QuestaoValidationHelper.normalizarModalidade(modalidadeBruta, request.alternativas());
        QuestaoValidationHelper.validarGabaritoPorModalidade(modalidade, gabaritoBruto);
        String gabaritoNormalizado = QuestaoValidationHelper.normalizarGabarito(modalidade, gabaritoBruto);

        Disciplina disciplina = disciplinaRepository.findById(request.disciplinaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Disciplina não encontrada no catálogo"
                ));

        Assunto assunto = assuntoRepository.findById(request.assuntoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Assunto não encontrado no catálogo"
                ));

        Banca banca = bancaRepository.findById(request.bancaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Banca não encontrada no catálogo"
                ));

        Instituicao instituicao = instituicaoRepository.findById(request.instituicaoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Instituição não encontrada no catálogo"
                ));

        Questao questao = new Questao();
        questao.setIdQuestion(QuestaoValidationHelper.gerarIdQuestion());
        questao.setEnunciado(request.enunciado());
        questao.setQuestao(request.questao());
        questao.setAlternativas(request.alternativas());
        questao.setAno(request.ano());
        questao.setCargo(request.cargo());
        questao.setNivel(request.nivel());
        questao.setModalidade(modalidade);
        questao.setGabarito(gabaritoNormalizado);

        questao.setDisciplinaCatalogo(disciplina);
        questao.setAssuntoCatalogo(assunto);
        questao.setBancaCatalogo(banca);
        questao.setInstituicaoCatalogo(instituicao);

        return repository.save(questao);
    }

    @Transactional
    public Questao atualizar(String idQuestion, QuestaoRequest request) {
        Questao questao = repository.findByIdQuestion(idQuestion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada"));

        String modalidadeBruta = request.modalidade().trim();
        String gabaritoBruto = request.gabarito().trim().toUpperCase();
        String modalidade = QuestaoValidationHelper.normalizarModalidade(modalidadeBruta, request.alternativas());
        QuestaoValidationHelper.validarGabaritoPorModalidade(modalidade, gabaritoBruto);
        String gabaritoNormalizado = QuestaoValidationHelper.normalizarGabarito(modalidade, gabaritoBruto);

        Disciplina disciplina = disciplinaRepository.findById(request.disciplinaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Disciplina não encontrada no catálogo"
                ));

        Assunto assunto = assuntoRepository.findById(request.assuntoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Assunto não encontrado no catálogo"
                ));

        Banca banca = bancaRepository.findById(request.bancaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Banca não encontrada no catálogo"
                ));

        Instituicao instituicao = instituicaoRepository.findById(request.instituicaoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Instituição não encontrada no catálogo"
                ));

        questao.setEnunciado(request.enunciado());
        questao.setQuestao(request.questao());
        questao.setAlternativas(request.alternativas());
        questao.setAno(request.ano());
        questao.setCargo(request.cargo());
        questao.setNivel(request.nivel());
        questao.setModalidade(modalidade);
        questao.setGabarito(gabaritoNormalizado);

        questao.setDisciplinaCatalogo(disciplina);
        questao.setAssuntoCatalogo(assunto);
        questao.setBancaCatalogo(banca);
        questao.setInstituicaoCatalogo(instituicao);

        return repository.save(questao);
    }

    @Transactional
    public void excluir(String idQuestion) {
        Questao questao = repository.findByIdQuestion(idQuestion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada"));
        repository.delete(questao);
    }

    @Transactional(readOnly = true)
    public Questao buscarPorIdQuestion(String idQuestion) {
        return repository.findDetalhadaByIdQuestion(idQuestion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada"));
    }

    @Transactional(readOnly = true)
    public Page<QuestaoResponse> listarFiltradoPaginado(
        String texto,
        Long disciplinaId,
        Long assuntoId,
        Long bancaId,
        Long instituicaoId,
        Integer ano,
        String cargo,
        String nivel,
        String modalidade,
        int page,
        int size,
        String sort
    ) {
        Specification<Questao> spec = Specification
            .where(QuestaoSpecifications.textoContains(texto))
            .and(QuestaoSpecifications.disciplinaIdEquals(disciplinaId))
            .and(QuestaoSpecifications.assuntoIdEquals(assuntoId))
            .and(QuestaoSpecifications.bancaIdEquals(bancaId))
            .and(QuestaoSpecifications.instituicaoIdEquals(instituicaoId))
            .and(QuestaoSpecifications.anoEquals(ano))
            .and(QuestaoSpecifications.cargoEquals(cargo))
            .and(QuestaoSpecifications.nivelEquals(nivel))
            .and(QuestaoSpecifications.modalidadeEquals(modalidade));

        PageRequest pageable = buildPageRequest(page, size, sort);
        return repository.findAll(spec, pageable)
            .map(QuestaoResponse::fromEntity);
      }

    private PageRequest buildPageRequest(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size);
        }

        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();

        if (!SORT_FIELDS_ALLOWED.contains(property)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "sort inválido. Permitidos: " + String.join(", ", SORT_FIELDS_ALLOWED)
            );
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length == 2 && parts[1].trim().equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }

        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}