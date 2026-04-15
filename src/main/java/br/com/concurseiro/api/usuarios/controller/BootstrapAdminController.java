package br.com.concurseiro.api.usuarios.controller;

import br.com.concurseiro.api.usuarios.model.Usuario;
import br.com.concurseiro.api.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import br.com.concurseiro.api.infra.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class BootstrapAdminController {

    private final UsuarioRepository usuarioRepository;

    @Value("${APP_ADMIN_API_KEY}")
    private String adminApiKey;

    public BootstrapAdminController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/bootstrap-admin")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<String> bootstrapAdmin(
            @RequestHeader("X-Admin-Key") String apiKey,
            @RequestParam String email,
            jakarta.servlet.http.HttpServletRequest request
    ) {
        if (!adminApiKey.equals(apiKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chave inválida");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        usuario.setRole(Usuario.Role.ADMIN);
        usuario.setStatus(Usuario.Status.ATIVO);
        usuarioRepository.save(usuario);

        return ApiResponse.success("Usuário promovido com sucesso", request.getRequestURI());
    }
}