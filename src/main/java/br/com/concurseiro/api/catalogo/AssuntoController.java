package br.com.concurseiro.api.catalogo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalogo/disciplinas/{disciplinaId}/assuntos")
public class AssuntoController {

    private final AssuntoRepository assuntoRepository;

    public AssuntoController(AssuntoRepository assuntoRepository) {
        this.assuntoRepository = assuntoRepository;
    }

    @GetMapping
    public List<AssuntoResponse> listar(@PathVariable Long disciplinaId) {
        return assuntoRepository
                .findByDisciplinaIdAndAtivoTrueOrderByNomeAsc(disciplinaId)
                .stream()
                .map(a -> new AssuntoResponse(a.getId(), a.getNome()))
                .toList();
    }

    public record AssuntoResponse(Long id, String nome) {}
}