package br.com.concurseiro.api.catalogo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/catalogo/bancas")
public class AdminBancaController {

    private final BancaRepository repository;

    public AdminBancaController(BancaRepository repository) {
        this.repository = repository;
    }

    public record BancaRequest(String nome) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Banca cadastrar(@RequestBody BancaRequest request) {

        if (request.nome() == null || request.nome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome é obrigatório");
        }

        if (repository.existsByNomeIgnoreCase(request.nome().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Banca já cadastrada");
        }

        Banca banca = new Banca();
        banca.setNome(request.nome().trim());

        return repository.save(banca);
    }
}