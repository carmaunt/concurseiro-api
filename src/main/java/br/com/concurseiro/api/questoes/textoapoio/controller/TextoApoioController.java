package br.com.concurseiro.api.questoes.textoapoio.controller;

import br.com.concurseiro.api.questoes.textoapoio.dto.TextoApoioRequest;
import br.com.concurseiro.api.questoes.textoapoio.dto.TextoApoioResponse;
import br.com.concurseiro.api.questoes.textoapoio.service.TextoApoioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Textos de apoio", description = "Cadastro e reutilização de textos base para questões")
@RestController
@RequestMapping("/api/v1/textos-apoio")
public class TextoApoioController {

    private final TextoApoioService service;

    public TextoApoioController(TextoApoioService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastrar ou reutilizar texto de apoio")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TextoApoioResponse cadastrar(@RequestBody @Valid TextoApoioRequest request) {
        return service.cadastrar(request);
    }

    @Operation(summary = "Buscar texto de apoio por ID")
    @GetMapping("/{id}")
    public TextoApoioResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @Operation(summary = "Listar textos de apoio")
    @GetMapping
    public Page<TextoApoioResponse> listar(
            @RequestParam(required = false) String titulo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.listar(titulo, page, size);
    }
}
