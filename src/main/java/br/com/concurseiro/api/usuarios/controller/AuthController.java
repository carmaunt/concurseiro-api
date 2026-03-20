package br.com.concurseiro.api.usuarios.controller;

import br.com.concurseiro.api.infra.security.JwtService;
import br.com.concurseiro.api.usuarios.dto.RefreshRequest;
import br.com.concurseiro.api.usuarios.dto.RefreshResponse;
import br.com.concurseiro.api.usuarios.model.Usuario;
import br.com.concurseiro.api.usuarios.repository.UsuarioRepository;
import br.com.concurseiro.api.usuarios.service.UsuarioService;
import br.com.concurseiro.api.usuarios.token.model.RefreshToken;
import br.com.concurseiro.api.usuarios.token.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Autenticacao", description = "Registro e login de usuarios")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService,
            RefreshTokenService refreshTokenService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.refreshTokenService = refreshTokenService;
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 160) String nome,
            @NotBlank @Email @Size(max = 200) String email,
            @NotBlank
            @Size(min = 8, max = 72)
            @jakarta.validation.constraints.Pattern(
                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s]).{8,72}$",
                    message = "a senha deve ter entre 8 e 72 caracteres, com letra minúscula, maiúscula, número e símbolo"
            )
            String senha
    ) {}

    public record LoginRequest(
            @NotBlank @Email @Size(max = 200) String email,
            @NotBlank
            @Size(min = 8, max = 72)
            @jakarta.validation.constraints.Pattern(
                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s]).{8,72}$",
                    message = "a senha deve ter entre 8 e 72 caracteres, com letra minúscula, maiúscula, número e símbolo"
            )
            String senha
    ) {}

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String email,
            String role
    ) {}

    @Operation(summary = "Registrar novo usuario")
    @SecurityRequirements
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody @Valid RegisterRequest req) {
        usuarioService.cadastrarVisitante(req.nome(), req.email(), req.senha());
    }

    @Operation(summary = "Autenticar e obter token JWT")
    @SecurityRequirements
    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest req) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.senha())
        );

        Usuario usuario = usuarioRepository.findByEmail(req.email()).orElseThrow();

        if (usuario.getStatus() != Usuario.Status.ATIVO) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuário ainda não aprovado"
            );
        }

        refreshTokenService.revogarTodosDoUsuario(usuario.getId());

        String accessToken = jwtService.generateToken(usuario.getEmail(), usuario.getRole());
        RefreshToken refreshToken = refreshTokenService.criar(usuario);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                usuario.getEmail(),
                usuario.getRole().name()
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody @Valid RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenService.buscarValido(request.refreshToken())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Refresh token inválido"
                ));

        Usuario usuario = refreshToken.getUsuario();

        refreshTokenService.revogar(request.refreshToken());
        RefreshToken novoRefreshToken = refreshTokenService.criar(usuario);

        String novoAccessToken = jwtService.generateToken(usuario.getEmail(), usuario.getRole());

        return ResponseEntity.ok(
                new RefreshResponse(novoAccessToken, novoRefreshToken.getToken())
        );
    }
}