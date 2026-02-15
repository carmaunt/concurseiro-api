package br.com.concurseiro.api.usuarios;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService service;

    public AdminUsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PatchMapping("/{id}/ativar")
    public UsuarioPublicoResponse ativar(@PathVariable Long id) {
        Usuario u = service.ativarUsuario(id);
        return UsuarioPublicoResponse.from(u);
    }
}