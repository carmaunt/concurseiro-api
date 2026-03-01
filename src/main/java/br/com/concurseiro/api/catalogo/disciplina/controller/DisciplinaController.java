package br.com.concurseiro.api.catalogo.disciplina.controller;

import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import br.com.concurseiro.api.catalogo.disciplina.service.DisciplinaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Catalogo", description = "Consulta publica de disciplinas, assuntos, bancas e instituicoes")
@RestController
@RequestMapping("/api/v1/catalogo/disciplinas")
public class DisciplinaController {

    private final DisciplinaService service;

    public DisciplinaController(DisciplinaService service) {
        this.service = service;
    }

    @Operation(summary = "Listar todas as disciplinas")
    @GetMapping
    public List<CatalogoItemResponse> listar() {
        return service.listar();
    }
}
