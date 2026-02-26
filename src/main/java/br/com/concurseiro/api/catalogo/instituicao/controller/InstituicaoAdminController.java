package br.com.concurseiro.api.catalogo.instituicao.controller;

import br.com.concurseiro.api.catalogo.instituicao.service.InstituicaoService;
import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/catalogo/instituicoes")
public class InstituicaoAdminController {

    private final InstituicaoService service;

    public InstituicaoAdminController(InstituicaoService service) {
        this.service = service;
    }

    public record InstituicaoRequest(
            @NotBlank @Size(max = 200) String nome
    ) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogoItemResponse cadastrar(@RequestBody @Valid InstituicaoRequest request) {
        return service.cadastrar(request.nome());
    }

    @GetMapping
    public List<CatalogoItemResponse> listar() {
        return service.listar();
    }
}