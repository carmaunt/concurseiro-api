package br.com.concurseiro.api.catalogo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogoItemResponse cadastrar(@RequestBody @Valid DisciplinaRequest request) {
        return service.cadastrar(request.nome());
    }
}