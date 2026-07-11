package br.com.concurseiro.api.conteudo.controller;

import br.com.concurseiro.api.conteudo.dto.TaxonomiaResumoResponse;
import br.com.concurseiro.api.conteudo.model.ConteudoPortal;
import br.com.concurseiro.api.conteudo.service.TaxonomiaPublicaService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TaxonomiaPublicaController {
    private final TaxonomiaPublicaService service;
    public TaxonomiaPublicaController(TaxonomiaPublicaService service) { this.service = service; }

    @GetMapping("/categorias/publicas")
    public List<TaxonomiaResumoResponse> categorias(@RequestParam ConteudoPortal.Tipo tipo) {
        return service.listarCategorias(tipo);
    }

    @GetMapping("/tags/publicas")
    public List<TaxonomiaResumoResponse> tags(@RequestParam ConteudoPortal.Tipo tipo) {
        return service.listarTags(tipo);
    }
}
