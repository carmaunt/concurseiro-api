package br.com.concurseiro.api.catalogo.instituicao.controller;

import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import br.com.concurseiro.api.catalogo.instituicao.service.InstituicaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Catalogo")
@RestController
@RequestMapping("/api/v1/catalogo/instituicoes")
public class CatalogoInstituicaoController {

    private final InstituicaoService service;

    public CatalogoInstituicaoController(InstituicaoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar todas as instituicoes")
    @GetMapping
    public List<CatalogoItemResponse> listar() {
        return service.listar();
    }
}
