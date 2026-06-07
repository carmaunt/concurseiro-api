package br.com.concurseiro.api.infra.security;

import br.com.concurseiro.api.usuarios.model.Usuario;
import br.com.concurseiro.api.usuarios.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final AuthCookieService authCookieService;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository, AuthCookieService authCookieService) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.authCookieService = authCookieService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String email = jwtService.extractEmail(token);
            String roleStr = jwtService.extractRole(token);

            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            if (usuario.getStatus() != Usuario.Status.ATIVO) {
                throw new IllegalStateException("Usuário não está ativo");
            }

            Usuario.Role tokenRole = Usuario.Role.valueOf(roleStr);

            if (usuario.getRole() != tokenRole) {
                throw new IllegalStateException("Role do token difere da role atual do usuário");
            }

            if (!jwtService.isValid(token, usuario.getEmail())) {
                throw new IllegalStateException("Token inválido");
            }

            var auth = new UsernamePasswordAuthenticationToken(
                    usuario.getEmail(),
                    null,
                    List.of(new SimpleGrantedAuthority(usuario.getRole().authority()))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring("Bearer ".length()).trim();
        }

        return authCookieService.getAccessToken(request).orElse(null);
    }
}
