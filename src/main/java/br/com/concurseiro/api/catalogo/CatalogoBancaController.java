package br.com.concurseiro.api.catalogo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalogo/bancas")
public class CatalogoBancaController {

    private final BancaRepository repository;

    public CatalogoBancaController(BancaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Banca> listar() {
        return repository.findAll();
    }
}