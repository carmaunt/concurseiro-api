package br.com.concurseiro.api.questoes;

import br.com.concurseiro.api.catalogo.Disciplina;
import br.com.concurseiro.api.catalogo.DisciplinaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import br.com.concurseiro.api.catalogo.Assunto;
import br.com.concurseiro.api.catalogo.AssuntoRepository;

import java.util.Set;
import java.util.UUID;

@Service
public class QuestaoService {
    
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

    public QuestaoService(
            QuestaoRepository repository,
            DisciplinaRepository disciplinaRepository,
            AssuntoRepository assuntoRepository
    ) {
        this.repository = repository;
        this.disciplinaRepository = disciplinaRepository;
        this.assuntoRepository = assuntoRepository;
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
        questao.setDisciplina(request.disciplina());
        questao.setAssunto(request.assunto());
        questao.setBanca(request.banca());
        questao.setInstituicao(request.instituicao());
        questao.setAno(request.ano());
        questao.setCargo(request.cargo());
        questao.setNivel(request.nivel());
        questao.setModalidade(modalidade);
        questao.setGabarito(gabaritoNormalizado);

        // ✅ NOVO: se vier disciplinaId, vincula a questão ao catálogo (migração gradual)
        if (request.disciplinaId() != null) {
            Disciplina disciplina = disciplinaRepository.findById(request.disciplinaId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Disciplina não encontrada no catálogo"
                    ));
            questao.setDisciplinaCatalogo(disciplina);
        }

        if (request.assuntoId() != null) {
            Assunto assunto = assuntoRepository.findById(request.assuntoId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Assunto não encontrado no catálogo"
                    ));
            questao.setAssuntoCatalogo(assunto);
        }

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

    @Transactional(readOnly = true)
    public Questao buscarPorIdQuestion(String idQuestion) {
        return repository.findByIdQuestion(idQuestion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada"));
    }

    @Transactional(readOnly = true)
    public Page<Questao> listarFiltradoPaginado(
            String texto,
            String disciplina,
            String assunto,
            String banca,
            String instituicao,
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
                .and(QuestaoSpecifications.disciplinaEquals(disciplina))
                .and(QuestaoSpecifications.assuntoEquals(assunto))
                .and(QuestaoSpecifications.bancaEquals(banca))
                .and(QuestaoSpecifications.instituicaoEquals(instituicao))
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