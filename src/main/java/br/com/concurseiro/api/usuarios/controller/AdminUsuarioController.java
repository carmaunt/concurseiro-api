package br.com.concurseiro.api.usuarios.controller;

import br.com.concurseiro.api.usuarios.model.Usuario;
import br.com.concurseiro.api.usuarios.dto.UsuarioPublicoResponse;
import br.com.concurseiro.api.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Usuarios", description = "Gerenciamento administrativo de usuarios")
@RestController
@RequestMapping("/api/v1/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService service;

    public AdminUsuarioController(UsuarioService service) {
        this.service = service;
    }

    @Operation(summary = "Listar todos os usuarios (paginado)")
    @GetMapping
    public Page<UsuarioPublicoResponse> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.listarPaginado(page, size);
    }

    @Operation(summary = "Ativar um usuario pendente")
    @PatchMapping("/{id}/ativar")
    public UsuarioPublicoResponse ativar(@PathVariable Long id) {
        Usuario u = service.ativarUsuario(id);
        return UsuarioPublicoResponse.from(u);
    }
}
