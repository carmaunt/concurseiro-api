package br.com.concurseiro.api.catalogo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalogo/assuntos/{assuntoId}/subassuntos")
public class SubAssuntoController {

    private final SubAssuntoService service;

    public SubAssuntoController(SubAssuntoService service) {
        this.service = service;
    }

    @GetMapping
    public List<CatalogoItemResponse> listar(@PathVariable Long assuntoId) {
        return service.listarPorAssunto(assuntoId);
    }
}