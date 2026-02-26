// src/main/java/br/com/concurseiro/api/admin/AdminQuestaoController.java
package br.com.concurseiro.api.admin;

import br.com.concurseiro.api.questoes.model.Questao;
import br.com.concurseiro.api.questoes.service.QuestaoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/questoes")
public class AdminQuestaoController {

    private final QuestaoService service;

    public AdminQuestaoController(QuestaoService service) {
        this.service = service;
    }

    @GetMapping("/{idQuestion}/gabarito")
    public GabaritoResponse buscarGabarito(@PathVariable String idQuestion) {
        Questao q = service.buscarPorIdQuestion(idQuestion);
        return new GabaritoResponse(q.getIdQuestion(), q.getGabarito());
    }

    public record GabaritoResponse(String idQuestion, String gabarito) {}
}