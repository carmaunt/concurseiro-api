package br.com.concurseiro.api.catalogo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalogo/bancas")
public class CatalogoBancaController {

    private final BancaService service;

    public CatalogoBancaController(BancaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CatalogoItemResponse> listar() {
        return service.listar();
    }
}