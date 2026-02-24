package br.com.concurseiro.api.catalogo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalogo/disciplinas")
public class DisciplinaController {

    private final DisciplinaRepository repository;

    public DisciplinaController(DisciplinaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<DisciplinaResponse> listar() {
        return repository.findAll().stream()
                .filter(Disciplina::isAtivo)
                .map(d -> new DisciplinaResponse(d.getId(), d.getNome()))
                .toList();
    }

    public record DisciplinaResponse(Long id, String nome) {}
}