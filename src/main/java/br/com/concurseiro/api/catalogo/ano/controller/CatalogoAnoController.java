package br.com.concurseiro.api.catalogo.ano.controller;

import br.com.concurseiro.api.questoes.repository.QuestaoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Catalogo")
@RestController
@RequestMapping("/api/v1/catalogo/anos")
public class CatalogoAnoController {

    private final QuestaoRepository questaoRepository;

    public CatalogoAnoController(QuestaoRepository questaoRepository) {
        this.questaoRepository = questaoRepository;
    }

    @Operation(summary = "Listar anos disponíveis nas questões")
    @GetMapping
    public List<Integer> listar() {
        return questaoRepository.findAnosDisponiveis();
    }
}
