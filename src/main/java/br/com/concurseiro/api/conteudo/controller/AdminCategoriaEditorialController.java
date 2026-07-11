package br.com.concurseiro.api.conteudo.controller;

import br.com.concurseiro.api.conteudo.dto.*;
import br.com.concurseiro.api.conteudo.model.StatusTaxonomia;
import br.com.concurseiro.api.conteudo.service.CategoriaEditorialService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/categorias-editoriais")
public class AdminCategoriaEditorialController {
    private final CategoriaEditorialService service;
    public AdminCategoriaEditorialController(CategoriaEditorialService service) { this.service = service; }

    @GetMapping
    public Page<CategoriaEditorialResponse> listar(@RequestParam(required = false, name = "q") String busca,
            @RequestParam(required = false) StatusTaxonomia status, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) { return service.listar(busca, status, page, size); }
    @GetMapping("/ativas")
    public List<TaxonomiaResumoResponse> listarAtivas() { return service.listarAtivas(); }
    @GetMapping("/{id}")
    public CategoriaEditorialResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CategoriaEditorialResponse criar(@RequestBody @Valid CategoriaEditorialRequest request) { return service.criar(request); }
    @PutMapping("/{id}")
    public CategoriaEditorialResponse atualizar(@PathVariable Long id, @RequestBody @Valid CategoriaEditorialRequest request) { return service.atualizar(id, request); }
    @PatchMapping("/{id}/status")
    public CategoriaEditorialResponse alterarStatus(@PathVariable Long id, @RequestParam StatusTaxonomia status) { return service.alterarStatus(id, status); }
}
