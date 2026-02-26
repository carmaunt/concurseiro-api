package br.com.concurseiro.api.usuarios.controller;

import br.com.concurseiro.api.infra.security.JwtService;
import br.com.concurseiro.api.usuarios.model.Usuario;
import br.com.concurseiro.api.usuarios.repository.UsuarioRepository;
import br.com.concurseiro.api.usuarios.service.UsuarioService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 160) String nome,
            @NotBlank @Email @Size(max = 200) String email,
            @NotBlank @Size(min = 6, max = 200) String senha
    ) {}

    public record LoginRequest(
            @NotBlank @Email @Size(max = 200) String email,
            @NotBlank @Size(min = 6, max = 200) String senha
    ) {}

    public record AuthResponse(
            String token,
            String email,
            String role
    ) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody RegisterRequest req) {
        usuarioService.cadastrarVisitante(req.nome(), req.email(), req.senha());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.email(),
                        req.senha()
                )
        );

        Usuario usuario = usuarioRepository.findByEmail(req.email()).orElseThrow();

        String token = jwtService.generateToken(
                usuario.getEmail(),
                usuario.getRole()
        );

        return new AuthResponse(
                token,
                usuario.getEmail(),
                usuario.getRole().name()
        );
    }
}