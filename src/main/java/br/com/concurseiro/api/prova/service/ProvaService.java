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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
                        HttpStatus.NOT_FOUND, "Instituição não encontrada no catálogo"));

        String modalidade = QuestaoValidationHelper.normalizarModalidade(request.modalidade().trim(), null);

        Prova prova = new Prova();
        prova.setBanca(request.banca().trim());
        prova.setInstituicao(inst.getNome());
        prova.setInstituicaoId(inst.getId());
        prova.setAno(request.ano());
        prova.setCargo(request.cargo().trim());
        prova.setNivel(request.nivel().trim());
        prova.setModalidade(modalidade);

        prova = provaRepository.save(prova);
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

        Questao questao = new Questao();
        questao.setIdQuestion(QuestaoValidationHelper.gerarIdQuestion());
        questao.setProvaId(provaId);
        questao.setEnunciado(request.enunciado());
        questao.setQuestao(request.questao());
        questao.setAlternativas(request.alternativas());
        questao.setBanca(prova.getBanca());
        questao.setInstituicao(prova.getInstituicao());
        questao.setAno(prova.getAno());
        questao.setCargo(prova.getCargo());
        questao.setNivel(prova.getNivel());
        questao.setModalidade(prova.getModalidade());
        questao.setGabarito(gabaritoNormalizado);

        Instituicao inst = instituicaoRepository.findById(prova.getInstituicaoId())
                .orElse(null);
        if (inst != null) {
            questao.setInstituicaoCatalogo(inst);
        }

        if (request.disciplinaId() != null) {
            Disciplina disciplina = disciplinaRepository.findById(request.disciplinaId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Disciplina não encontrada no catálogo"));
            questao.setDisciplinaCatalogo(disciplina);
            questao.setDisciplina(disciplina.getNome());
        } else {
            questao.setDisciplina(request.disciplina());
        }

        if (request.assuntoId() != null) {
            Assunto assunto = assuntoRepository.findById(request.assuntoId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Assunto não encontrado no catálogo"));
            questao.setAssuntoCatalogo(assunto);
            questao.setAssunto(assunto.getNome());
        } else {
            questao.setAssunto(request.assunto());
        }

        return questaoRepository.save(questao);
    }
}
