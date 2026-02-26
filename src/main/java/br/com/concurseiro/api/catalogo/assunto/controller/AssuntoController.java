package br.com.concurseiro.api.catalogo.assunto.controller;

import br.com.concurseiro.api.catalogo.assunto.service.AssuntoService;
import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogo/disciplinas/{disciplinaId}/assuntos")
public class AssuntoController {

    private final AssuntoService service;

    public AssuntoController(AssuntoService service) {
        this.service = service;
    }

    @GetMapping
    public List<CatalogoItemResponse> listar(@PathVariable Long disciplinaId) {
        return service.listarPorDisciplina(disciplinaId);
    }
}