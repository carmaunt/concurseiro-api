package br.com.concurseiro.api.catalogo.banca.controller;

import br.com.concurseiro.api.catalogo.banca.service.BancaService;
import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Catalogo")
@RestController
@RequestMapping("/api/v1/catalogo/bancas")
public class CatalogoBancaController {

    private final BancaService service;

    public CatalogoBancaController(BancaService service) {
        this.service = service;
    }

    @Operation(summary = "Listar todas as bancas examinadoras")
    @GetMapping
    public List<CatalogoItemResponse> listar() {
        return service.listar();
    }
}
