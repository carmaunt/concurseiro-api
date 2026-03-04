package br.com.concurseiro.api.comentario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Comentarios", description = "Comentários de questões")
@RestController
@RequestMapping("/api/v1")
public class ComentarioController {

    private final ComentarioRepository comentarioRepository;

    public ComentarioController(ComentarioRepository comentarioRepository) {
        this.comentarioRepository = comentarioRepository;
    }

    @Operation(summary = "Listar comentários de uma questão")
    @GetMapping("/questoes/{questaoId}/comentarios")
    public Page<ComentarioResponse> listarComentarios(
            @PathVariable String questaoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "curtidas") String ordenar
    ) {
        Sort sort;
        if ("recentes".equals(ordenar)) {
            sort = Sort.by(Sort.Direction.DESC, "criadoEm");
        } else {
            sort = Sort.by(Sort.Direction.DESC, "curtidas");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Comentario> comentarios = comentarioRepository.findByQuestaoId(questaoId, pageable);
        return comentarios.map(ComentarioResponse::fromEntity);
    }

    @Operation(summary = "Criar comentário em uma questão")
    @PostMapping("/questoes/{questaoId}/comentarios")
    @ResponseStatus(HttpStatus.CREATED)
    public ComentarioResponse criarComentario(
            @PathVariable String questaoId,
            @Valid @RequestBody ComentarioRequest request
    ) {
        Comentario comentario = new Comentario(questaoId, request.autor(), request.texto());
        Comentario salvo = comentarioRepository.save(comentario);
        return ComentarioResponse.fromEntity(salvo);
    }

    @Operation(summary = "Curtir um comentário")
    @PostMapping("/comentarios/{id}/curtir")
    public ComentarioResponse curtir(@PathVariable Long id) {
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado"));
        comentario.setCurtidas(comentario.getCurtidas() + 1);
        Comentario salvo = comentarioRepository.save(comentario);
        return ComentarioResponse.fromEntity(salvo);
    }

    @Operation(summary = "Descurtir um comentário")
    @PostMapping("/comentarios/{id}/descurtir")
    public ComentarioResponse descurtir(@PathVariable Long id) {
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado"));
        comentario.setDescurtidas(comentario.getDescurtidas() + 1);
        Comentario salvo = comentarioRepository.save(comentario);
        return ComentarioResponse.fromEntity(salvo);
    }
}
