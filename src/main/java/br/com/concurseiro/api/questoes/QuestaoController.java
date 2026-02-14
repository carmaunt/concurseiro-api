package br.com.concurseiro.api.questoes;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/questoes")
public class QuestaoController {

    private static final int MAX_PAGE_SIZE = 50;

    private final QuestaoService service;

    public QuestaoController(QuestaoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestaoResponse cadastrar(@RequestBody @Valid QuestaoRequest request) {
        return QuestaoResponse.fromEntity(service.cadastrar(request));
    }

    @GetMapping("/{idQuestion}")
    public QuestaoResponse buscar(@PathVariable String idQuestion) {
        return QuestaoResponse.fromEntity(service.buscarPorIdQuestion(idQuestion));
    }

    @GetMapping
    public Page<QuestaoResponse> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String disciplina,
            @RequestParam(required = false) String assunto,
            @RequestParam(required = false) String banca,
            @RequestParam(required = false) String instituicao,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String cargo,
            @RequestParam(required = false) String nivel,
            @RequestParam(required = false) String modalidade,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "page não pode ser negativa"
            );
        }

        if (size < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "size deve ser maior que zero"
            );
        }

        if (size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "size máximo permitido é " + MAX_PAGE_SIZE
            );
        }

        return service.listarFiltradoPaginado(
                        texto,
                        disciplina,
                        assunto,
                        banca,
                        instituicao,
                        ano,
                        cargo,
                        nivel,
                        modalidade,
                        page,
                        size,
                        sort
                )
                .map(QuestaoResponse::fromEntity);
    }
}