package br.com.concurseiro.api.admin;

import br.com.concurseiro.api.questoes.dto.QuestaoRequest;
import br.com.concurseiro.api.questoes.dto.QuestaoResponse;
import br.com.concurseiro.api.questoes.model.Questao;
import br.com.concurseiro.api.questoes.service.QuestaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Questoes", description = "Gerenciamento administrativo de questoes")
@RestController
@RequestMapping("/api/v1/admin/questoes")
public class AdminQuestaoController {

    private final QuestaoService service;

    public AdminQuestaoController(QuestaoService service) {
        this.service = service;
    }

    @Operation(summary = "Consultar gabarito de uma questao")
    @GetMapping("/{idQuestion}/gabarito")
    public GabaritoResponse buscarGabarito(@PathVariable String idQuestion) {
        Questao q = service.buscarPorIdQuestion(idQuestion);
        return new GabaritoResponse(q.getIdQuestion(), q.getGabarito());
    }

    @Operation(summary = "Atualizar uma questao existente")
    @PutMapping("/{idQuestion}")
    public QuestaoResponse atualizar(
            @PathVariable String idQuestion,
            @RequestBody @Valid QuestaoRequest request) {
        return QuestaoResponse.fromEntity(service.atualizar(idQuestion, request));
    }

    @Operation(summary = "Excluir uma questao")
    @DeleteMapping("/{idQuestion}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable String idQuestion) {
        service.excluir(idQuestion);
    }

    public record GabaritoResponse(String idQuestion, String gabarito) {}
}
