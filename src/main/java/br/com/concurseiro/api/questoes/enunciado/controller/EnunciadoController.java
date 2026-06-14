package br.com.concurseiro.api.questoes.enunciado.controller;

import br.com.concurseiro.api.questoes.enunciado.dto.EnunciadoResponse;
import br.com.concurseiro.api.questoes.enunciado.service.EnunciadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Enunciados", description = "Consulta de enunciados reutilizáveis")
@RestController
@RequestMapping("/api/v1/enunciados")
public class EnunciadoController {

    private final EnunciadoService service;

    public EnunciadoController(EnunciadoService service) {
        this.service = service;
    }

    @Operation(summary = "Buscar enunciado por ID")
    @GetMapping("/{id}")
    public EnunciadoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @Operation(summary = "Listar enunciados reutilizáveis")
    @GetMapping
    public Page<EnunciadoResponse> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return service.listar(texto, page, size);
    }
}
