package br.com.concurseiro.api.catalogo.banca.controller;

import br.com.concurseiro.api.catalogo.banca.service.BancaService;
import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogoItemResponse cadastrar(@RequestBody @Valid BancaRequest request) {
        return service.cadastrar(request.nome());
    }
}