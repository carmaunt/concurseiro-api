package br.com.concurseiro.api.catalogo.banca.controller;

import br.com.concurseiro.api.catalogo.banca.service.BancaService;
import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogo/bancas")
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