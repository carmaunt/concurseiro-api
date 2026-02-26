package br.com.concurseiro.api.catalogo.subassunto.controller;

import br.com.concurseiro.api.catalogo.subassunto.service.SubAssuntoService;
import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/catalogo/subassuntos")
public class SubAssuntoAdminController {

    private final SubAssuntoService service;

    public SubAssuntoAdminController(SubAssuntoService service) {
        this.service = service;
    }

    public record SubAssuntoRequest(
            @NotNull Long assuntoId,
            @NotBlank @Size(max = 200) String nome
    ) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogoItemResponse cadastrar(@RequestBody @Valid SubAssuntoRequest request) {
        return service.cadastrar(request.assuntoId(), request.nome());
    }
}