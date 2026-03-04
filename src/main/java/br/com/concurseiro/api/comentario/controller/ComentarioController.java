package br.com.concurseiro.api.comentario.controller;

import br.com.concurseiro.api.comentario.dto.ComentarioRequest;
import br.com.concurseiro.api.comentario.dto.ComentarioResponse;
import br.com.concurseiro.api.comentario.model.Comentario;
import br.com.concurseiro.api.comentario.repository.ComentarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Comentarios", description = "Comentários em questões de concursos")
@RestController
public class ComentarioController {

    private static final int MAX_PAGE_SIZE = 50;

    private final ComentarioRepository repository;

    public ComentarioController(ComentarioRepository repository) {
        this.repository = repository;
    }

    @Operation(summary = "Listar comentários de uma questão")
    @GetMapping("/api/v1/questoes/{questaoId}/comentarios")
    public Page<ComentarioResponse> listar(
            @PathVariable String questaoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "recentes") String ordenar) {

        if (size > MAX_PAGE_SIZE) size = MAX_PAGE_SIZE;

        Sort sort;
        if ("curtidas".equalsIgnoreCase(ordenar)) {
            sort = Sort.by(Sort.Direction.DESC, "curtidas");
        } else {
            sort = Sort.by(Sort.Direction.DESC, "criadoEm");
        }

        return repository.findByQuestaoId(questaoId, PageRequest.of(page, size, sort))
                .map(ComentarioResponse::fromEntity);
    }

    @Operation(summary = "Adicionar comentário em uma questão")
    @PostMapping("/api/v1/questoes/{questaoId}/comentarios")
    @ResponseStatus(HttpStatus.CREATED)
    public ComentarioResponse criar(
            @PathVariable String questaoId,
            @RequestBody @Valid ComentarioRequest request) {

        Comentario c = new Comentario();
        c.setQuestaoId(questaoId);
        c.setAutor(request.autor().trim());
        c.setTexto(request.texto().trim());

        return ComentarioResponse.fromEntity(repository.save(c));
    }

    @Operation(summary = "Curtir um comentário")
    @PostMapping("/api/v1/comentarios/{id}/curtir")
    public ComentarioResponse curtir(@PathVariable Long id) {
        Comentario c = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado"));
        c.setCurtidas(c.getCurtidas() + 1);
        return ComentarioResponse.fromEntity(repository.save(c));
    }

    @Operation(summary = "Descurtir um comentário")
    @PostMapping("/api/v1/comentarios/{id}/descurtir")
    public ComentarioResponse descurtir(@PathVariable Long id) {
        Comentario c = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado"));
        c.setDescurtidas(c.getDescurtidas() + 1);
        return ComentarioResponse.fromEntity(repository.save(c));
    }
}
