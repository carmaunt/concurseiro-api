package br.com.concurseiro.api.catalogo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/catalogo/disciplinas")
public class DisciplinaAdminController {

    private final DisciplinaRepository repository;

    public DisciplinaAdminController(DisciplinaRepository repository) {
        this.repository = repository;
    }

    public record DisciplinaRequest(
            @NotBlank
            @Size(max = 160)
            String nome
    ) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void cadastrar(@RequestBody DisciplinaRequest request) {

        if (repository.existsByNomeIgnoreCase(request.nome())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Disciplina já cadastrada"
            );
        }

        Disciplina d = new Disciplina();
        d.setNome(request.nome().trim());

        repository.save(d);
    }
}