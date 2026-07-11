package br.com.concurseiro.api.conteudo.controller;

import br.com.concurseiro.api.conteudo.dto.ConteudoPortalResponse;
import br.com.concurseiro.api.conteudo.model.ConteudoPortal;
import br.com.concurseiro.api.conteudo.service.ConteudoPortalService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/conteudos")
public class ConteudoPortalController {

    private final ConteudoPortalService service;

    public ConteudoPortalController(ConteudoPortalService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ConteudoPortalResponse> listar(
            @RequestParam(required = false) ConteudoPortal.Tipo tipo,
            @RequestParam(required = false, name = "q") String busca,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer limit
    ) {
        return service.listarPublicados(tipo, busca != null ? busca : search, categoria, category, tag,
                page, limit != null ? limit : size);
    }

    @GetMapping("/destaques")
    public Page<ConteudoPortalResponse> destaques(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return service.listarDestaques(page, size);
    }

    @GetMapping("/{tipo}/{slug}")
    public ConteudoPortalResponse buscar(@PathVariable ConteudoPortal.Tipo tipo, @PathVariable String slug) {
        return service.buscarPublicado(tipo, slug);
    }
}
