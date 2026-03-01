package br.com.concurseiro.api.catalogo.disciplina.controller;

import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import br.com.concurseiro.api.catalogo.disciplina.service.DisciplinaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Disciplinas", description = "CRUD administrativo de disciplinas")
@RestController
@RequestMapping("/api/v1/admin/catalogo/disciplinas")
public class DisciplinaAdminController {

    private final DisciplinaService service;

    public DisciplinaAdminController(DisciplinaService service) {
        this.service = service;
    }

    public record DisciplinaRequest(
            @NotBlank @Size(max = 200) String nome
    ) {}

    @Operation(summary = "Cadastrar nova disciplina")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogoItemResponse cadastrar(@RequestBody @Valid DisciplinaRequest request) {
        return service.cadastrar(request.nome());
    }

    @Operation(summary = "Atualizar disciplina existente")
    @PutMapping("/{id}")
    public CatalogoItemResponse atualizar(@PathVariable Long id, @RequestBody @Valid DisciplinaRequest request) {
        return service.atualizar(id, request.nome());
    }

    @Operation(summary = "Excluir disciplina")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
