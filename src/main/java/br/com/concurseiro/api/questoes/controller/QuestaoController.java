package br.com.concurseiro.api.questoes.controller;

import br.com.concurseiro.api.questoes.dto.QuestaoRequest;
import br.com.concurseiro.api.questoes.dto.QuestaoResponse;
import br.com.concurseiro.api.questoes.dto.QuestaoWebResponse;
import br.com.concurseiro.api.questoes.resposta.dto.RespostaQuestaoRequest;
import br.com.concurseiro.api.questoes.resposta.dto.RespostaQuestaoResponse;
import br.com.concurseiro.api.questoes.resposta.service.RespostaQuestaoUsuarioService;
import br.com.concurseiro.api.questoes.service.QuestaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Questoes", description = "Consulta e cadastro de questoes de concursos")
@RestController
@RequestMapping("/api/v1/questoes")
public class QuestaoController {

    private static final int MAX_PAGE_SIZE = 50;

    private final QuestaoService service;
    private final RespostaQuestaoUsuarioService respostaService;

    public QuestaoController(QuestaoService service, RespostaQuestaoUsuarioService respostaService) {
        this.service = service;
        this.respostaService = respostaService;
    }

    @Operation(summary = "Cadastrar nova questao")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestaoResponse cadastrar(@RequestBody @Valid QuestaoRequest request) {
        return QuestaoResponse.fromEntity(service.cadastrar(request));
    }

    @Operation(summary = "Buscar questao por ID")
    @GetMapping("/{idQuestion}")
    public QuestaoResponse buscar(@PathVariable String idQuestion) {
        return QuestaoResponse.fromEntity(service.buscarPorIdQuestion(idQuestion));
    }

    @Operation(summary = "Listar questoes com filtros e paginacao")
    @GetMapping
    public Page<QuestaoResponse> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long disciplinaId,
            @RequestParam(required = false) Long assuntoId,
            @RequestParam(required = false) Long subassuntoId,
            @RequestParam(required = false) Long bancaId,
            @RequestParam(required = false) Long instituicaoId,
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
                disciplinaId,
                assuntoId,
                subassuntoId,
                bancaId,
                instituicaoId,
                ano,
                cargo,
                nivel,
                modalidade,
                page,
                size,
                sort
        );
    }

    @Operation(summary = "Listar questoes para resolucao na web sem expor gabarito")
    @GetMapping("/web")
    public Page<QuestaoWebResponse> listarParaWeb(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long disciplinaId,
            @RequestParam(required = false) Long assuntoId,
            @RequestParam(required = false) Long subassuntoId,
            @RequestParam(required = false) Long bancaId,
            @RequestParam(required = false) Long instituicaoId,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String cargo,
            @RequestParam(required = false) String nivel,
            @RequestParam(required = false) String modalidade,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        validarPaginacao(page, size);

        return service.listarParaWeb(
                texto,
                disciplinaId,
                assuntoId,
                subassuntoId,
                bancaId,
                instituicaoId,
                ano,
                cargo,
                nivel,
                modalidade,
                page,
                size,
                sort
        );
    }

    @Operation(summary = "Buscar questao para resolucao na web sem expor gabarito")
    @GetMapping("/web/{idQuestion}")
    public QuestaoWebResponse buscarParaWeb(@PathVariable String idQuestion) {
        return service.buscarParaWeb(idQuestion);
    }

    @Operation(summary = "Responder questao na web e registrar historico do usuario")
    @PostMapping("/web/{idQuestion}/respostas")
    public RespostaQuestaoResponse responderParaWeb(
            @PathVariable String idQuestion,
            @RequestBody @Valid RespostaQuestaoRequest request,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        return respostaService.responder(authentication.getName(), idQuestion, request);
    }

    @Operation(summary = "Buscar ultima resposta do usuario para uma questao")
    @GetMapping("/web/{idQuestion}/respostas/ultima")
    public RespostaQuestaoResponse buscarUltimaRespostaParaWeb(
            @PathVariable String idQuestion,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        return respostaService.buscarUltima(authentication.getName(), idQuestion);
    }

    private void validarPaginacao(int page, int size) {
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
    }
}
