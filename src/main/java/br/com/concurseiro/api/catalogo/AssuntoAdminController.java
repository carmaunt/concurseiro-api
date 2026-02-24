package br.com.concurseiro.api.catalogo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/catalogo/assuntos")
public class AssuntoAdminController {

    private final AssuntoRepository assuntoRepository;
    private final DisciplinaRepository disciplinaRepository;

    public AssuntoAdminController(
            AssuntoRepository assuntoRepository,
            DisciplinaRepository disciplinaRepository
    ) {
        this.assuntoRepository = assuntoRepository;
        this.disciplinaRepository = disciplinaRepository;
    }

    public record AssuntoRequest(
            @NotNull Long disciplinaId,
            @NotBlank @Size(max = 200) String nome
    ) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void cadastrar(@RequestBody AssuntoRequest request) {

        var disciplina = disciplinaRepository.findById(request.disciplinaId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada")
                );

        if (assuntoRepository.existsByDisciplinaIdAndNomeIgnoreCase(
                request.disciplinaId(), request.nome())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Assunto já cadastrado para esta disciplina"
            );
        }

        Assunto assunto = new Assunto();
        assunto.setDisciplina(disciplina);
        assunto.setNome(request.nome().trim());

        assuntoRepository.save(assunto);
    }
}