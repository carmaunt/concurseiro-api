package br.com.concurseiro.api.prova.service;

import br.com.concurseiro.api.catalogo.assunto.model.Assunto;
import br.com.concurseiro.api.catalogo.assunto.repository.AssuntoRepository;
import br.com.concurseiro.api.catalogo.disciplina.model.Disciplina;
import br.com.concurseiro.api.catalogo.disciplina.repository.DisciplinaRepository;
import br.com.concurseiro.api.catalogo.instituicao.model.Instituicao;
import br.com.concurseiro.api.catalogo.instituicao.repository.InstituicaoRepository;
import br.com.concurseiro.api.prova.dto.ProvaQuestaoRequest;
import br.com.concurseiro.api.prova.dto.ProvaRequest;
import br.com.concurseiro.api.prova.dto.ProvaResponse;
import br.com.concurseiro.api.prova.model.Prova;
import br.com.concurseiro.api.prova.repository.ProvaRepository;
import br.com.concurseiro.api.questoes.model.Questao;
import br.com.concurseiro.api.questoes.repository.QuestaoRepository;
import br.com.concurseiro.api.questoes.service.QuestaoValidationHelper;

import java.net.URI;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProvaService {

    private final ProvaRepository provaRepository;
    private final QuestaoRepository questaoRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final AssuntoRepository assuntoRepository;

    public ProvaService(
        ProvaRepository provaRepository,
        QuestaoRepository questaoRepository,
        InstituicaoRepository instituicaoRepository,
        DisciplinaRepository disciplinaRepository,
        AssuntoRepository assuntoRepository
    ) {
        this.provaRepository = provaRepository;
        this.questaoRepository = questaoRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.assuntoRepository = assuntoRepository;
    }

    @Transactional
    public ProvaResponse criarProva(ProvaRequest request) {
        Instituicao inst = instituicaoRepository.findById(request.instituicaoId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Instituição não encontrada no catálogo"
            ));

        String banca = normalizarTextoLivre(request.banca());
        String cargo = normalizarTextoLivre(request.cargo());
        String nivel = normalizarTextoLivre(request.nivel());
        String modalidade = QuestaoValidationHelper.normalizarModalidade(request.modalidade().trim(), null);

        boolean jaExiste = provaRepository
            .existsByBancaIgnoreCaseAndInstituicaoIdAndAnoAndCargoIgnoreCaseAndNivelIgnoreCaseAndModalidadeIgnoreCase(
                banca,
                inst.getId(),
                request.ano(),
                cargo,
                nivel,
                modalidade
            );

        if (jaExiste) {
            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
            pd.setTitle("Prova duplicada");
            pd.setType(URI.create("https://concurseiro.dev/errors/prova-duplicada"));
            pd.setDetail("Já existe uma prova cadastrada com esse cabeçalho");
            throw new ErrorResponseException(HttpStatus.CONFLICT, pd, null);
        }

        Prova prova = new Prova();
        prova.setBanca(banca);
        prova.setInstituicao(inst.getNome());
        prova.setInstituicaoId(inst.getId());
        prova.setAno(request.ano());
        prova.setCargo(cargo);
        prova.setNivel(nivel);
        prova.setModalidade(modalidade);

        try {
            prova = provaRepository.save(prova);
        } catch (DataIntegrityViolationException ex) {
            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
            pd.setTitle("Prova duplicada");
            pd.setType(URI.create("https://concurseiro.dev/errors/prova-duplicada"));
            pd.setDetail("Já existe uma prova cadastrada com esse cabeçalho");
            throw new ErrorResponseException(HttpStatus.CONFLICT, pd, ex);
        }

        return ProvaResponse.fromEntity(prova, 0L);
    }

    @Transactional(readOnly = true)
    public ProvaResponse buscarPorId(Long id) {
        Prova prova = provaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prova não encontrada"));

        long total = questaoRepository.countByProvaId(id);
        return ProvaResponse.fromEntity(prova, total);
    }

    @Transactional(readOnly = true)
    public Page<ProvaResponse> listar(int page, int size) {
        return provaRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "criadoEm")))
            .map(p -> ProvaResponse.fromEntity(p, questaoRepository.countByProvaId(p.getId())));
    }

    @Transactional
    public Questao cadastrarQuestao(Long provaId, ProvaQuestaoRequest request) {
        Prova prova = provaRepository.findById(provaId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prova não encontrada"));

        String gabaritoBruto = request.gabarito().trim().toUpperCase();
        QuestaoValidationHelper.validarGabaritoPorModalidade(prova.getModalidade(), gabaritoBruto);
        String gabaritoNormalizado = QuestaoValidationHelper.normalizarGabarito(prova.getModalidade(), gabaritoBruto);

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

        Instituicao instituicao = instituicaoRepository.findById(prova.getInstituicaoId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Instituição não encontrada no catálogo"
            ));

        Questao questao = new Questao();
        questao.setIdQuestion(QuestaoValidationHelper.gerarIdQuestion());
        questao.setProvaId(provaId);
        questao.setEnunciado(request.enunciado());
        questao.setQuestao(request.questao());
        questao.setAlternativas(request.alternativas());
        questao.setAno(prova.getAno());
        questao.setCargo(prova.getCargo());
        questao.setNivel(prova.getNivel());
        questao.setModalidade(prova.getModalidade());
        questao.setGabarito(gabaritoNormalizado);

        questao.setInstituicaoCatalogo(instituicao);
        questao.setDisciplinaCatalogo(disciplina);
        questao.setAssuntoCatalogo(assunto);

        return questaoRepository.save(questao);
    }

    private String normalizarTextoLivre(String valor) {
        if (valor == null) return null;
        return valor.trim().replaceAll("\\s+", " ");
    }
}