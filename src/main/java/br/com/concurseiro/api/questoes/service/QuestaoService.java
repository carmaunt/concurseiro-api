package br.com.concurseiro.api.questoes.service;

import br.com.concurseiro.api.catalogo.disciplina.model.Disciplina;
import br.com.concurseiro.api.catalogo.disciplina.repository.DisciplinaRepository;
import br.com.concurseiro.api.questoes.model.Questao;
import br.com.concurseiro.api.questoes.repository.QuestaoRepository;
import br.com.concurseiro.api.questoes.dto.QuestaoRequest;
import br.com.concurseiro.api.questoes.spec.QuestaoSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import br.com.concurseiro.api.catalogo.assunto.model.Assunto;
import br.com.concurseiro.api.catalogo.assunto.repository.AssuntoRepository;
import br.com.concurseiro.api.catalogo.banca.model.Banca;
import br.com.concurseiro.api.catalogo.banca.repository.BancaRepository;
import br.com.concurseiro.api.catalogo.instituicao.model.Instituicao;
import br.com.concurseiro.api.catalogo.instituicao.repository.InstituicaoRepository;

import java.util.Set;
import java.util.UUID;

@Service
public class QuestaoService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QuestaoService.class);
    private static final Set<String> SORT_FIELDS_ALLOWED = Set.of(
            "ano",
            "criadoEm",
            "disciplina",
            "banca",
            "instituicao"
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

        String modalidade = normalizarModalidade(modalidadeBruta, request.alternativas());

        validarGabaritoPorModalidade(modalidade, gabaritoBruto);

        String gabaritoNormalizado = normalizarGabarito(modalidade, gabaritoBruto);

        Questao questao = new Questao();
        questao.setIdQuestion(gerarIdQuestion()); // ✅ agora sempre vem preenchido ANTES do INSERT
        questao.setEnunciado(request.enunciado());
        questao.setQuestao(request.questao());
        questao.setAlternativas(request.alternativas());
        questao.setBanca(request.banca());
        questao.setInstituicao(request.instituicao());
        questao.setAno(request.ano());
        questao.setCargo(request.cargo());
        questao.setNivel(request.nivel());
        questao.setModalidade(modalidade);
        questao.setGabarito(gabaritoNormalizado);

        // ===== Disciplina (fonte de verdade quando vier disciplinaId) =====
        if (request.disciplinaId() != null) {
            Disciplina disciplina = disciplinaRepository.findById(request.disciplinaId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Disciplina não encontrada no catálogo"
                    ));
            questao.setDisciplinaCatalogo(disciplina);
            questao.setDisciplina(disciplina.getNome()); // <-- texto passa a ser derivado do catálogo
        } else {
            questao.setDisciplina(request.disciplina());
        }

        // ===== Assunto (fonte de verdade quando vier assuntoId) =====
        if (request.assuntoId() != null) {
            Assunto assunto = assuntoRepository.findById(request.assuntoId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Assunto não encontrado no catálogo"
                    ));
            questao.setAssuntoCatalogo(assunto);
            questao.setAssunto(assunto.getNome()); // <-- texto passa a ser derivado do catálogo
        } else {
            questao.setAssunto(request.assunto());
        }

        // ===== Banca (bloco bancaId) =====

        if (request.bancaId() != null) {
            Banca banca = bancaRepository.findById(request.bancaId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Banca não encontrada no catálogo"
                    ));
            questao.setBancaCatalogo(banca);
            questao.setBanca(banca.getNome()); // texto deriva do catálogo
        } else {
            questao.setBanca(request.banca());
        }

        // ===== Instituição (fonte de verdade quando vier instituicaoId) =====
        if (request.instituicaoId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "instituicaoId é obrigatório"
            );
        }

        Instituicao inst = instituicaoRepository.findById(request.instituicaoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Instituição não encontrada no catálogo"
                ));

        questao.setInstituicaoCatalogo(inst);
        questao.setInstituicao(inst.getNome());

        return repository.save(questao);
    }

    private String gerarIdQuestion() {
        // Coluna tem length=16. Vamos gerar algo como: Q + 15 chars hex => total 16
        // Ex: Q4F1A2B3C4D5E6F7
        String hex = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return "Q" + hex.substring(0, 15);
    }

    private String normalizarModalidade(String modalidadeBruta, String alternativas) {
        String m = modalidadeBruta.trim().toUpperCase();

        if (m.equals("MÚLTIPLA ESCOLHA") || m.equals("MULTIPLA ESCOLHA")) {
            return alternativas != null && alternativas.toUpperCase().contains("E)")
                    ? "A_E"
                    : "A_D";
        }

        if (m.equals("CERTO E ERRADO") || m.equals("CERTO/ERRADO")) {
            return "CERTO_ERRADO";
        }

        return m;
    }

    private void validarGabaritoPorModalidade(String modalidade, String gabarito) {
        boolean ok = switch (modalidade) {
            case "A_E" -> gabarito.matches("^[A-E]$");
            case "A_D" -> gabarito.matches("^[A-D]$");
            case "CERTO_ERRADO" -> gabarito.equals("C") || gabarito.equals("E")
                    || gabarito.equals("CERTO") || gabarito.equals("ERRADO");
            default -> false;
        };

        if (!ok) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Modalidade inválida ou gabarito incompatível (modalidade=" + modalidade + ")"
            );
        }
    }

    private String normalizarGabarito(String modalidade, String gabarito) {
        if (!"CERTO_ERRADO".equals(modalidade)) return gabarito;

        return switch (gabarito) {
            case "CERTO" -> "C";
            case "ERRADO" -> "E";
            default -> gabarito;
        };
    }

    @Transactional
    public Questao atualizar(String idQuestion, QuestaoRequest request) {
        Questao questao = repository.findByIdQuestion(idQuestion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada"));

        String modalidadeBruta = request.modalidade().trim();
        String gabaritoBruto = request.gabarito().trim().toUpperCase();
        String modalidade = normalizarModalidade(modalidadeBruta, request.alternativas());
        validarGabaritoPorModalidade(modalidade, gabaritoBruto);
        String gabaritoNormalizado = normalizarGabarito(modalidade, gabaritoBruto);

        questao.setEnunciado(request.enunciado());
        questao.setQuestao(request.questao());
        questao.setAlternativas(request.alternativas());
        questao.setAno(request.ano());
        questao.setCargo(request.cargo());
        questao.setNivel(request.nivel());
        questao.setModalidade(modalidade);
        questao.setGabarito(gabaritoNormalizado);

        if (request.disciplinaId() != null) {
            Disciplina disciplina = disciplinaRepository.findById(request.disciplinaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada no catálogo"));
            questao.setDisciplinaCatalogo(disciplina);
            questao.setDisciplina(disciplina.getNome());
        } else {
            questao.setDisciplina(request.disciplina());
        }

        if (request.assuntoId() != null) {
            Assunto assunto = assuntoRepository.findById(request.assuntoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assunto não encontrado no catálogo"));
            questao.setAssuntoCatalogo(assunto);
            questao.setAssunto(assunto.getNome());
        } else {
            questao.setAssunto(request.assunto());
        }

        if (request.bancaId() != null) {
            Banca banca = bancaRepository.findById(request.bancaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Banca não encontrada no catálogo"));
            questao.setBancaCatalogo(banca);
            questao.setBanca(banca.getNome());
        } else {
            questao.setBanca(request.banca());
        }

        if (request.instituicaoId() != null) {
            Instituicao inst = instituicaoRepository.findById(request.instituicaoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instituição não encontrada no catálogo"));
            questao.setInstituicaoCatalogo(inst);
            questao.setInstituicao(inst.getNome());
        } else {
            questao.setInstituicao(request.instituicao());
        }

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
        return repository.findByIdQuestion(idQuestion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada"));
    }

    @Transactional(readOnly = true)
    public Page<Questao> listarFiltradoPaginado(
            String texto,
            String disciplina,
            Long disciplinaId,
            String assunto,
            Long assuntoId,
            String banca,
            Long bancaId,
            String instituicao,
            Long instituicaoId,
            Integer ano,
            String cargo,
            String nivel,
            String modalidade,
            int page,
            int size,
            String sort
    ) {

        if (disciplinaId == null && disciplina != null && !disciplina.isBlank()) {
            log.warn("Filtro por texto usado: disciplina='{}' (prefira disciplinaId)", disciplina);
        }
        if (assuntoId == null && assunto != null && !assunto.isBlank()) {
            log.warn("Filtro por texto usado: assunto='{}' (prefira assuntoId)", assunto);
        }
        if (bancaId == null && banca != null && !banca.isBlank()) {
            log.warn("Filtro por texto usado: banca='{}' (prefira bancaId)", banca);
        }
        if (instituicaoId == null && instituicao != null && !instituicao.isBlank()) {
            log.warn("Filtro por texto usado: instituicao='{}' (prefira instituicaoId)", instituicao);
        }
        Specification<Questao> spec = Specification
                .where(QuestaoSpecifications.textoContains(texto))

                // ===== Disciplina =====
                .and(disciplinaId != null
                        ? QuestaoSpecifications.disciplinaIdEquals(disciplinaId)
                        : QuestaoSpecifications.disciplinaEquals(disciplina))

                // ===== Assunto =====
                .and(assuntoId != null
                        ? QuestaoSpecifications.assuntoIdEquals(assuntoId)
                        : QuestaoSpecifications.assuntoEquals(assunto))

                // ===== Banca =====
                .and(bancaId != null
                        ? QuestaoSpecifications.bancaIdEquals(bancaId)
                        : QuestaoSpecifications.bancaEquals(banca))

                // ===== Demais filtros =====
                .and(instituicaoId != null
                        ? QuestaoSpecifications.instituicaoIdEquals(instituicaoId)
                        : QuestaoSpecifications.instituicaoEquals(instituicao))
                .and(QuestaoSpecifications.anoEquals(ano))
                .and(QuestaoSpecifications.cargoEquals(cargo))
                .and(QuestaoSpecifications.nivelEquals(nivel))
                .and(QuestaoSpecifications.modalidadeEquals(modalidade));

        PageRequest pageable = buildPageRequest(page, size, sort);

        return repository.findAll(spec, pageable);
    }

    private PageRequest buildPageRequest(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size);
        }

        // sort=ano,desc | sort=criadoEm,asc
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