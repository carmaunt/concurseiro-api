package br.com.concurseiro.api.catalogo.instituicao.controller;

import br.com.concurseiro.api.catalogo.instituicao.service.InstituicaoService;
import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin - Instituicoes", description = "CRUD administrativo de instituicoes")
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

    @Operation(summary = "Cadastrar nova instituicao")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogoItemResponse cadastrar(@RequestBody @Valid InstituicaoRequest request) {
        return service.cadastrar(request.nome());
    }

    @Operation(summary = "Listar todas as instituicoes")
    @GetMapping
    public List<CatalogoItemResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Atualizar instituicao existente")
    @PutMapping("/{id}")
    public CatalogoItemResponse atualizar(@PathVariable Long id, @RequestBody @Valid InstituicaoRequest request) {
        return service.atualizar(id, request.nome());
    }

    @Operation(summary = "Excluir instituicao")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
