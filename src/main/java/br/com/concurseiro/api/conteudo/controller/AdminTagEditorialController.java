package br.com.concurseiro.api.conteudo.controller;

import br.com.concurseiro.api.conteudo.dto.*;
import br.com.concurseiro.api.conteudo.model.StatusTaxonomia;
import br.com.concurseiro.api.conteudo.service.TagEditorialService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/tags-editoriais")
public class AdminTagEditorialController {
    private final TagEditorialService service;
    public AdminTagEditorialController(TagEditorialService service) { this.service = service; }

    @GetMapping
    public Page<TagEditorialResponse> listar(@RequestParam(required = false, name = "q") String busca,
            @RequestParam(required = false) StatusTaxonomia status, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) { return service.listar(busca, status, page, size); }
    @GetMapping("/ativas")
    public List<TaxonomiaResumoResponse> listarAtivas(@RequestParam(required = false, name = "q") String busca) { return service.listarAtivas(busca); }
    @GetMapping("/{id}")
    public TagEditorialResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public TagEditorialResponse criar(@RequestBody @Valid TagEditorialRequest request) { return service.criar(request); }
    @PutMapping("/{id}")
    public TagEditorialResponse atualizar(@PathVariable Long id, @RequestBody @Valid TagEditorialRequest request) { return service.atualizar(id, request); }
    @PatchMapping("/{id}/status")
    public TagEditorialResponse alterarStatus(@PathVariable Long id, @RequestParam StatusTaxonomia status) { return service.alterarStatus(id, status); }
}
