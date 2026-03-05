package br.com.concurseiro.api.prova.controller;

import br.com.concurseiro.api.prova.dto.ProvaQuestaoRequest;
import br.com.concurseiro.api.prova.dto.ProvaRequest;
import br.com.concurseiro.api.prova.dto.ProvaResponse;
import br.com.concurseiro.api.prova.service.ProvaService;
import br.com.concurseiro.api.questoes.dto.QuestaoResponse;
import br.com.concurseiro.api.questoes.model.Questao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Provas", description = "Gerenciamento de provas de concursos")
@RestController
@RequestMapping("/api/v1/provas")
public class ProvaController {

    private final ProvaService service;

    public ProvaController(ProvaService service) {
        this.service = service;
    }

    @Operation(summary = "Criar cabeçalho de prova")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProvaResponse criar(@RequestBody @Valid ProvaRequest request) {
        return service.criarProva(request);
    }

    @Operation(summary = "Buscar prova por ID")
    @GetMapping("/{id}")
    public ProvaResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @Operation(summary = "Listar provas cadastradas")
    @GetMapping
    public Page<ProvaResponse> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.listar(page, size);
    }

    @Operation(summary = "Lançar questão em uma prova")
    @PostMapping("/{provaId}/questoes")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestaoResponse lancarQuestao(
            @PathVariable Long provaId,
            @RequestBody @Valid ProvaQuestaoRequest request) {
        Questao questao = service.cadastrarQuestao(provaId, request);
        return QuestaoResponse.fromEntity(questao);
    }
}
