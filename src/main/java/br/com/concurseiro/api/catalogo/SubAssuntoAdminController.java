package br.com.concurseiro.api.catalogo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/catalogo/subassuntos")
public class SubAssuntoAdminController {

    private final SubAssuntoRepository subAssuntoRepository;
    private final AssuntoRepository assuntoRepository;

    public SubAssuntoAdminController(
            SubAssuntoRepository subAssuntoRepository,
            AssuntoRepository assuntoRepository
    ) {
        this.subAssuntoRepository = subAssuntoRepository;
        this.assuntoRepository = assuntoRepository;
    }

    public record SubAssuntoRequest(
            @NotNull Long assuntoId,
            @NotBlank @Size(max = 200) String nome
    ) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void cadastrar(@RequestBody SubAssuntoRequest request) {

        var assunto = assuntoRepository.findById(request.assuntoId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Assunto não encontrado")
                );

        if (subAssuntoRepository.existsByAssuntoIdAndNomeIgnoreCase(
                request.assuntoId(), request.nome())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Subassunto já cadastrado para este assunto"
            );
        }

        SubAssunto sub = new SubAssunto();
        sub.setAssunto(assunto);
        sub.setNome(request.nome().trim());

        subAssuntoRepository.save(sub);
    }
}