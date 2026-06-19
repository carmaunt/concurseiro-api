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
import br.com.concurseiro.api.questoes.dto.QuestaoResponse;
import br.com.concurseiro.api.questoes.enunciado.model.Enunciado;
import br.com.concurseiro.api.questoes.enunciado.service.EnunciadoService;
import br.com.concurseiro.api.questoes.model.Questao;
import br.com.concurseiro.api.questoes.repository.QuestaoRepository;
import br.com.concurseiro.api.questoes.spec.QuestaoSpecifications;
import br.com.concurseiro.api.questoes.textoapoio.model.TextoApoio;
import br.com.concurseiro.api.questoes.textoapoio.service.TextoApoioService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final Set<String> SORT_FIELDS_ALLOWED = Set.of("ano", "criadoEm");

    private final QuestaoRepository repository;
    private final DisciplinaRepository disciplinaRepository;
    private final AssuntoRepository assuntoRepository;
    private final SubAssuntoRepository subAssuntoRepository;
    private final BancaRepository bancaRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final EnunciadoService enunciadoService;
    private final TextoApoioService textoApoioService;

    public QuestaoService(QuestaoRepository repository, DisciplinaRepository disciplinaRepository, AssuntoRepository assuntoRepository, BancaRepository bancaRepository, InstituicaoRepository instituicaoRepository) {
        this(repository, disciplinaRepository, assuntoRepository, null, bancaRepository, instituicaoRepository, null, null);
    }

    @Autowired
    public QuestaoService(QuestaoRepository repository, DisciplinaRepository disciplinaRepository, AssuntoRepository assuntoRepository, SubAssuntoRepository subAssuntoRepository, BancaRepository bancaRepository, InstituicaoRepository instituicaoRepository, EnunciadoService enunciadoService, TextoApoioService textoApoioService) {
        this.repository = repository;
        this.disciplinaRepository = disciplinaRepository;
        this.assuntoRepository = assuntoRepository;
        this.subAssuntoRepository = subAssuntoRepository;
        this.bancaRepository = bancaRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.enunciadoService = enunciadoService;
        this.textoApoioService = textoApoioService;
    }

    @Transactional
    public Questao cadastrar(QuestaoRequest request) {
        String modalidade = QuestaoValidationHelper.normalizarModalidade(request.modalidade().trim(), request.alternativas());
        String gabaritoBruto = request.gabarito().trim().toUpperCase();
        QuestaoValidationHelper.validarGabaritoPorModalidade(modalidade, gabaritoBruto);
        String gabaritoNormalizado = QuestaoValidationHelper.normalizarGabarito(modalidade, gabaritoBruto);

        Disciplina disciplina = disciplinaRepository.findById(request.disciplinaId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada no catálogo"));
        Assunto assunto = assuntoRepository.findById(request.assuntoId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assunto não encontrado no catálogo"));
        SubAssunto subAssunto = resolverSubAssunto(request.subassuntoId(), assunto);
        Banca banca = bancaRepository.findById(request.bancaId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Banca não encontrada no catálogo"));
        Instituicao instituicao = instituicaoRepository.findById(request.instituicaoId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instituição não encontrada no catálogo"));
        TextoApoio textoApoio = resolverTextoApoio(
                request.textoApoioId(),
                request.textoApoioTitulo(),
                request.textoApoioTipo(),
                request.textoApoioConteudo(),
                request.textoApoioJson()
        );
        Enunciado enunciado = resolverEnunciado(request.enunciadoId(), request.enunciado());

        Questao questao = new Questao();
        questao.setIdQuestion(QuestaoValidationHelper.gerarIdQuestion());
        questao.setEnunciadoCatalogo(enunciado);
        questao.setQuestao(request.questao());
        questao.setAlternativas(request.alternativas());
        questao.setExplicacao(normalizarCampoOpcional(request.explicacao()));
        questao.setTextoApoio(textoApoio);
        questao.setAno(request.ano());
        questao.setCargo(request.cargo());
        questao.setNivel(request.nivel());
        questao.setModalidade(modalidade);
        questao.setGabarito(gabaritoNormalizado);
        questao.setDisciplinaCatalogo(disciplina);
        questao.setAssuntoCatalogo(assunto);
        questao.setSubAssuntoCatalogo(subAssunto);
        questao.setBancaCatalogo(banca);
        questao.setInstituicaoCatalogo(instituicao);

        return repository.save(questao);
    }

    @Transactional
    public Questao atualizar(String idQuestion, QuestaoRequest request) {
        Questao questao = repository.findByIdQuestion(idQuestion).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada"));
        String modalidade = QuestaoValidationHelper.normalizarModalidade(request.modalidade().trim(), request.alternativas());
        String gabaritoBruto = request.gabarito().trim().toUpperCase();
        QuestaoValidationHelper.validarGabaritoPorModalidade(modalidade, gabaritoBruto);
        String gabaritoNormalizado = QuestaoValidationHelper.normalizarGabarito(modalidade, gabaritoBruto);

        Disciplina disciplina = disciplinaRepository.findById(request.disciplinaId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada no catálogo"));
        Assunto assunto = assuntoRepository.findById(request.assuntoId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assunto não encontrado no catálogo"));
        SubAssunto subAssunto = resolverSubAssunto(request.subassuntoId(), assunto);
        Banca banca = bancaRepository.findById(request.bancaId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Banca não encontrada no catálogo"));
        Instituicao instituicao = instituicaoRepository.findById(request.instituicaoId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instituição não encontrada no catálogo"));
        TextoApoio textoApoio = resolverTextoApoio(
                request.textoApoioId(),
                request.textoApoioTitulo(),
                request.textoApoioTipo(),
                request.textoApoioConteudo(),
                request.textoApoioJson()
        );
        Enunciado enunciado = resolverEnunciado(request.enunciadoId(), request.enunciado());

        questao.setEnunciadoCatalogo(enunciado);
        questao.setQuestao(request.questao());
        questao.setAlternativas(request.alternativas());
        questao.setExplicacao(normalizarCampoOpcional(request.explicacao()));
        questao.setTextoApoio(textoApoio);
        questao.setAno(request.ano());
        questao.setCargo(request.cargo());
        questao.setNivel(request.nivel());
        questao.setModalidade(modalidade);
        questao.setGabarito(gabaritoNormalizado);
        questao.setDisciplinaCatalogo(disciplina);
        questao.setAssuntoCatalogo(assunto);
        questao.setSubAssuntoCatalogo(subAssunto);
        questao.setBancaCatalogo(banca);
        questao.setInstituicaoCatalogo(instituicao);

        return repository.save(questao);
    }

    @Transactional
    public void excluir(String idQuestion) {
        Questao questao = repository.findByIdQuestion(idQuestion).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada"));
        repository.delete(questao);
    }

    @Transactional(readOnly = true)
    public Questao buscarPorIdQuestion(String idQuestion) {
        return repository.findDetalhadaByIdQuestion(idQuestion).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada"));
    }

    @Transactional(readOnly = true)
    public Page<QuestaoResponse> listarFiltradoPaginado(String texto, Long disciplinaId, Long assuntoId, Long subassuntoId, Long bancaId, Long instituicaoId, Integer ano, String cargo, String nivel, String modalidade, int page, int size, String sort) {
        Specification<Questao> spec = Specification.where(QuestaoSpecifications.textoContains(texto))
                .and(QuestaoSpecifications.disciplinaIdEquals(disciplinaId))
                .and(QuestaoSpecifications.assuntoIdEquals(assuntoId))
                .and(QuestaoSpecifications.subassuntoIdEquals(subassuntoId))
                .and(QuestaoSpecifications.bancaIdEquals(bancaId))
                .and(QuestaoSpecifications.instituicaoIdEquals(instituicaoId))
                .and(QuestaoSpecifications.anoEquals(ano))
                .and(QuestaoSpecifications.cargoEquals(cargo))
                .and(QuestaoSpecifications.nivelEquals(nivel))
                .and(QuestaoSpecifications.modalidadeEquals(modalidade));

        return repository.findAll(spec, buildPageRequest(page, size, sort)).map(QuestaoResponse::fromEntity);
    }

    private TextoApoio resolverTextoApoio(Long textoApoioId, String titulo, String tipo, String conteudo, String conteudoJson) {
        if (textoApoioService == null) return null;
        return textoApoioService.resolverTextoApoio(textoApoioId, titulo, tipo, conteudo, conteudoJson);
    }

    private Enunciado resolverEnunciado(Long enunciadoId, String conteudo) {
        if (enunciadoService != null) {
            return enunciadoService.resolverEnunciado(enunciadoId, conteudo);
        }

        Enunciado enunciado = new Enunciado();
        enunciado.setConteudo(normalizarCampoOpcional(conteudo));
        enunciado.setHashSha256("teste");
        return enunciado;
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

    private String normalizarCampoOpcional(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private PageRequest buildPageRequest(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) return PageRequest.of(page, size);
        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();
        if (!SORT_FIELDS_ALLOWED.contains(property)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sort inválido. Permitidos: " + String.join(", ", SORT_FIELDS_ALLOWED));
        Sort.Direction direction = parts.length == 2 && parts[1].trim().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
