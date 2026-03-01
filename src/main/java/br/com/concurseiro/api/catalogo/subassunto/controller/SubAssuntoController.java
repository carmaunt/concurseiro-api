package br.com.concurseiro.api.catalogo.subassunto.controller;

import br.com.concurseiro.api.catalogo.subassunto.service.SubAssuntoService;
import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Catalogo")
@RestController
@RequestMapping("/api/v1/catalogo/assuntos/{assuntoId}/subassuntos")
public class SubAssuntoController {

    private final SubAssuntoService service;

    public SubAssuntoController(SubAssuntoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar subassuntos de um assunto")
    @GetMapping
    public List<CatalogoItemResponse> listar(@PathVariable Long assuntoId) {
        return service.listarPorAssunto(assuntoId);
    }
}
