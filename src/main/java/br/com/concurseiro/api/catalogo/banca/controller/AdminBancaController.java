package br.com.concurseiro.api.catalogo.banca.controller;

import br.com.concurseiro.api.catalogo.banca.service.BancaService;
import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Bancas", description = "CRUD administrativo de bancas examinadoras")
@RestController
@RequestMapping("/api/v1/admin/catalogo/bancas")
public class AdminBancaController {

    private final BancaService service;

    public AdminBancaController(BancaService service) {
        this.service = service;
    }

    public record BancaRequest(
            @NotBlank @Size(max = 160) String nome
    ) {}

    @Operation(summary = "Cadastrar nova banca")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogoItemResponse cadastrar(@RequestBody @Valid BancaRequest request) {
        return service.cadastrar(request.nome());
    }

    @Operation(summary = "Atualizar banca existente")
    @PutMapping("/{id}")
    public CatalogoItemResponse atualizar(@PathVariable Long id, @RequestBody @Valid BancaRequest request) {
        return service.atualizar(id, request.nome());
    }

    @Operation(summary = "Excluir banca")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
