package br.com.concurseiro.api.catalogo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalogo/assuntos/{assuntoId}/subassuntos")
public class SubAssuntoController {

    private final SubAssuntoRepository repository;

    public SubAssuntoController(SubAssuntoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<SubAssuntoResponse> listar(@PathVariable Long assuntoId) {
        return repository
                .findByAssuntoIdAndAtivoTrueOrderByNomeAsc(assuntoId)
                .stream()
                .map(s -> new SubAssuntoResponse(s.getId(), s.getNome()))
                .toList();
    }

    public record SubAssuntoResponse(Long id, String nome) {}
}