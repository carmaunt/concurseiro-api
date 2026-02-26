// src/main/java/br/com/concurseiro/api/catalogo/CatalogoInstituicaoController.java
package br.com.concurseiro.api.catalogo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogo/instituicoes")
public class CatalogoInstituicaoController {

    private final InstituicaoRepository repository;

    public CatalogoInstituicaoController(InstituicaoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Instituicao> listar() {
        return repository.findAll();
    }
}