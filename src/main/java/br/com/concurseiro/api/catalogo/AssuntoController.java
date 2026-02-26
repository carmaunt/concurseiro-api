package br.com.concurseiro.api.catalogo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalogo/disciplinas/{disciplinaId}/assuntos")
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