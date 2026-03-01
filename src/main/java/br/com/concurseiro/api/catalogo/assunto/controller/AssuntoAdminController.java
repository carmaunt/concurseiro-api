package br.com.concurseiro.api.catalogo.assunto.controller;

import br.com.concurseiro.api.catalogo.assunto.service.AssuntoService;
import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Assuntos", description = "CRUD administrativo de assuntos")
@RestController
@RequestMapping("/api/v1/admin/catalogo/assuntos")
public class AssuntoAdminController {

    private final AssuntoService service;

    public AssuntoAdminController(AssuntoService service) {
        this.service = service;
    }

    public record AssuntoRequest(
            @NotNull Long disciplinaId,
            @NotBlank @Size(max = 200) String nome
    ) {}

    @Operation(summary = "Cadastrar novo assunto vinculado a uma disciplina")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogoItemResponse cadastrar(@RequestBody @Valid AssuntoRequest request) {
        return service.cadastrar(request.disciplinaId(), request.nome());
    }
}
