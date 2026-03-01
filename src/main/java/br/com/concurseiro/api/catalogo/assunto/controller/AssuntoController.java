package br.com.concurseiro.api.catalogo.assunto.controller;

import br.com.concurseiro.api.catalogo.assunto.service.AssuntoService;
import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Catalogo")
@RestController
@RequestMapping("/api/v1/catalogo/disciplinas/{disciplinaId}/assuntos")
public class AssuntoController {

    private final AssuntoService service;

    public AssuntoController(AssuntoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar assuntos de uma disciplina")
    @GetMapping
    public List<CatalogoItemResponse> listar(@PathVariable Long disciplinaId) {
        return service.listarPorDisciplina(disciplinaId);
    }
}
