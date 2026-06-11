package br.com.concurseiro.api.prova.service;

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
import br.com.concurseiro.api.prova.dto.ProvaQuestaoRequest;
import br.com.concurseiro.api.prova.dto.ProvaRequest;
import br.com.concurseiro.api.prova.dto.ProvaResponse;
import br.com.concurseiro.api.prova.model.Prova;
import br.com.concurseiro.api.prova.repository.ProvaRepository;
import br.com.concurseiro.api.questoes.model.Questao;
import br.com.concurseiro.api.questoes.repository.QuestaoRepository;
import br.com.concurseiro.api.questoes.service.QuestaoValidationHelper;
import br.com.concurseiro.api.questoes.textoapoio.model.TextoApoio;
import br.com.concurseiro.api.questoes.textoapoio.service.TextoApoioService;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.net.URI;

@Service
public class ProvaService {
    private final ProvaRepository provaRepository;
    private final QuestaoRepository questaoRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final AssuntoRepository assuntoRepository;
    private final SubAssuntoRepository subAssuntoRepository;
    private final BancaRepository bancaRepository;
    private final TextoApoioService textoApoioService;

    public ProvaService(ProvaRepository provaRepository, QuestaoRepository questaoRepository, InstituicaoRepository instituicaoRepository, DisciplinaRepository disciplinaRepository, AssuntoRepository assuntoRepository, BancaRepository bancaRepository) {
        this(provaRepository, questaoRepository, instituicaoRepository, disciplinaRepository, assuntoRepository, null, bancaRepository, null);
    }

    @Autowired
    public ProvaService(ProvaRepository provaRepository, QuestaoRepository questaoRepository, InstituicaoRepository instituicaoRepository, DisciplinaRepository disciplinaRepository, AssuntoRepository assuntoRepository, SubAssuntoRepository subAssuntoRepository, BancaRepository bancaRepository, TextoApoioService textoApoioService) {
        this.provaRepository = provaRepository;
        this.questaoRepository = questaoRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.assuntoRepository = assuntoRepository;
        this.subAssuntoRepository = subAssuntoRepository;
        this.bancaRepository = bancaRepository;
        this.textoApoioService = textoApoioService;
    }

    @Transactional
    public ProvaResponse criarProva(ProvaRequest request) {
        Instituicao inst = instituicaoRepository.findById(request.instituicaoId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instituição não encontrada no catálogo"));
        String banca = normalizarTextoLivre(request.banca());
        String cargo = normalizarTextoLivre(request.cargo());
        String nivel = normalizarTextoLivre(request.nivel());
        String modalidade = QuestaoValidationHelper.normalizarModalidade(request.modalidade().trim(), null);
        boolean jaExiste = provaRepository.existsByBancaIgnoreCaseAndInstituicaoCatalogoIdAndAnoAndCargoIgnoreCaseAndNivelIgnoreCaseAndModalidadeIgnoreCase(banca, inst.getId(), request.ano(), cargo, nivel, modalidade);
        if (jaExiste) throw conflitoProvaDuplicada(null);
        Prova prova = new Prova();
        prova.setBanca(banca);
        prova.setInstituicaoCatalogo(inst);
        prova.setAno(request.ano());
        prova.setCargo(cargo);
        prova.setNivel(nivel);
        prova.setModalidade(modalidade);
        try {
            prova = provaRepository.save(prova);
        } catch (DataIntegrityViolationException ex) {
            throw conflitoProvaDuplicada(ex);
        }
        return ProvaResponse.fromEntity(prova, 0L);
    }

    @Transactional(readOnly = true)
    public ProvaResponse buscarPorId(Long id) {
        Prova prova = provaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prova não encontrada"));
        return ProvaResponse.fromEntity(prova, questaoRepository.countByProvaId(id));
    }

    @Transactional(readOnly = true)
    public Page<ProvaResponse> listar(int page, int size) {
        return provaRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "criadoEm"))).map(p -> ProvaResponse.fromEntity(p, questaoRepository.countByProvaId(p.getId())));
    }

    @Transactional
    public Questao cadastrarQuestao(Long provaId, ProvaQuestaoRequest request) {
        Prova prova = provaRepository.findById(provaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prova não encontrada"));
        String gabaritoBruto = request.gabarito().trim().toUpperCase();
        QuestaoValidationHelper.validarGabaritoPorModalidade(prova.getModalidade(), gabaritoBruto);
        String gabaritoNormalizado = QuestaoValidationHelper.normalizarGabarito(prova.getModalidade(), gabaritoBruto);
        Disciplina disciplina = disciplinaRepository.findById(request.disciplinaId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada no catálogo"));
        Assunto assunto = assuntoRepository.findById(request.assuntoId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assunto não encontrado no catálogo"));
        SubAssunto subAssunto = resolverSubAssunto(request.subassuntoId(), assunto);
        Instituicao instituicao = instituicaoRepository.findById(prova.getInstituicaoId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instituição não encontrada no catálogo"));
        Banca banca = bancaRepository.findByNomeIgnoreCase(prova.getBanca()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Banca não encontrada no catálogo"));
        TextoApoio textoApoio = resolverTextoApoio(
                request.textoApoioId(),
                request.textoApoioTitulo(),
                request.textoApoioTipo(),
                request.textoApoioConteudo(),
                request.textoApoioJson()
        );
        Questao questao = new Questao();
        questao.setIdQuestion(QuestaoValidationHelper.gerarIdQuestion());
        questao.setProvaId(provaId);
        questao.setEnunciado(normalizarCampoOpcional(request.enunciado()));
        questao.setQuestao(request.questao());
        questao.setAlternativas(request.alternativas());
        questao.setTextoApoio(textoApoio);
        questao.setAno(prova.getAno());
        questao.setCargo(prova.getCargo());
        questao.setNivel(prova.getNivel());
        questao.setModalidade(prova.getModalidade());
        questao.setGabarito(gabaritoNormalizado);
        questao.setInstituicaoCatalogo(instituicao);
        questao.setDisciplinaCatalogo(disciplina);
        questao.setAssuntoCatalogo(assunto);
        questao.setSubAssuntoCatalogo(subAssunto);
        questao.setBancaCatalogo(banca);
        return questaoRepository.save(questao);
    }

    private TextoApoio resolverTextoApoio(Long textoApoioId, String titulo, String tipo, String conteudo, String conteudoJson) {
        if (textoApoioService == null) return null;
        return textoApoioService.resolverTextoApoio(textoApoioId, titulo, tipo, conteudo, conteudoJson);
    }

    private String normalizarCampoOpcional(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private SubAssunto resolverSubAssunto(Long subassuntoId, Assunto assunto) {
        if (subassuntoId == null) return null;
        if (subAssuntoRepository == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Subassunto não encontrado no catálogo");
        }

        SubAssunto subAssunto = subAssuntoRepository.findById(subassuntoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subassunto não encontrado no catálogo"));

        Long assuntoDoSubAssunto = subAssunto.getAssunto() != null ? subAssunto.getAssunto().getId() : null;
        if (!assunto.getId().equals(assuntoDoSubAssunto)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subassunto não pertence ao assunto informado");
        }

        return subAssunto;
    }

    private ErrorResponseException conflitoProvaDuplicada(Throwable cause) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Prova duplicada");
        pd.setType(URI.create("https://concurseiro.dev/errors/prova-duplicada"));
        pd.setDetail("Já existe uma prova cadastrada com esse cabeçalho");
        return new ErrorResponseException(HttpStatus.CONFLICT, pd, cause);
    }

    private String normalizarTextoLivre(String valor) {
        if (valor == null) return null;
        return valor.trim().replaceAll("\\s+", " ");
    }

    @Transactional
    public void excluir(Long id) {
        Prova prova = provaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prova não encontrada"));
        provaRepository.delete(prova);
    }
}
