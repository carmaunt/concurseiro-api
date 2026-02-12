package br.com.concurseiro.api.admin;

import br.com.concurseiro.api.questoes.Questao;
import br.com.concurseiro.api.questoes.QuestaoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/questoes")
public class AdminQuestaoController {

    private final QuestaoService service;

    @Value("${app.admin.api-key}")
    private String apiKey;

    public AdminQuestaoController(QuestaoService service) {
        this.service = service;
    }

    @GetMapping("/{idQuestion}/gabarito")
    public GabaritoResponse buscarGabarito(
            @PathVariable String idQuestion,
            @RequestHeader(name = "X-API-KEY", required = false) String providedKey
    ) {
        if (providedKey == null || !providedKey.equals(apiKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chave inválida");
        }

        Questao q = service.buscarPorIdQuestion(idQuestion);
        return new GabaritoResponse(q.getIdQuestion(), q.getGabarito());
    }

    public record GabaritoResponse(String idQuestion, String gabarito) {}
}