package br.com.concurseiro.api.conteudo.controller;

import br.com.concurseiro.api.conteudo.dto.ConteudoPortalRequest;
import br.com.concurseiro.api.conteudo.dto.ConteudoPortalResponse;
import br.com.concurseiro.api.conteudo.model.ConteudoPortal;
import br.com.concurseiro.api.conteudo.service.ConteudoPortalService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/conteudos")
public class AdminConteudoPortalController {

    private final ConteudoPortalService service;

    public AdminConteudoPortalController(ConteudoPortalService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ConteudoPortalResponse> listar(
            @RequestParam(required = false) ConteudoPortal.Tipo tipo,
            @RequestParam(required = false) ConteudoPortal.Status status,
            @RequestParam(required = false, name = "q") String busca,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.listarAdmin(tipo, status, busca, page, size);
    }

    @GetMapping("/{id}")
    public ConteudoPortalResponse buscar(@PathVariable Long id) {
        return service.buscarAdmin(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConteudoPortalResponse criar(@RequestBody @Valid ConteudoPortalRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    public ConteudoPortalResponse atualizar(@PathVariable Long id, @RequestBody @Valid ConteudoPortalRequest request) {
        return service.atualizar(id, request);
    }

    @PatchMapping("/{id}/status")
    public ConteudoPortalResponse alterarStatus(@PathVariable Long id, @RequestParam ConteudoPortal.Status status) {
        return service.alterarStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
