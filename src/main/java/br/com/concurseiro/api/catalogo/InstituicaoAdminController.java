// src/main/java/br/com/concurseiro/api/catalogo/InstituicaoAdminController.java
package br.com.concurseiro.api.catalogo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/admin/catalogo/instituicoes")
public class InstituicaoAdminController {

    private final InstituicaoRepository repository;

    public InstituicaoAdminController(InstituicaoRepository repository) {
        this.repository = repository;
    }

    public record InstituicaoRequest(
            @NotBlank @Size(max = 200) String nome
    ) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void cadastrar(@RequestBody InstituicaoRequest request) {

        String nome = request.nome().trim();

        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Instituição já cadastrada"
            );
        }

        Instituicao inst = new Instituicao();
        inst.setNome(nome);

        repository.save(inst);
    }

    @GetMapping
    public List<Instituicao> listar() {
        return repository.findAll();
    }
}