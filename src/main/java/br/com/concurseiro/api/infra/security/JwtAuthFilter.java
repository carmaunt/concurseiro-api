package br.com.concurseiro.api.infra.security;

import br.com.concurseiro.api.usuarios.model.Usuario;
import br.com.concurseiro.api.usuarios.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(
            JwtService jwtService,
            UsuarioRepository usuarioRepository,
            ObjectMapper objectMapper
    ) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();

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

            var authorities = List.of(
                    new SimpleGrantedAuthority(usuario.getRole().authority())
            );

            var authentication = new UsernamePasswordAuthenticationToken(
                    usuario,
                    null,
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, request, ex.getMessage());
        }
    }

    private void writeUnauthorized(
            HttpServletResponse response,
            HttpServletRequest request,
            String detail
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", URI.create("https://httpstatuses.com/401").toString());
        body.put("title", "Unauthorized");
        body.put("status", 401);
        body.put("detail", detail == null || detail.isBlank() ? "Falha na autenticação" : detail);
        body.put("instance", request.getRequestURI());

        objectMapper.writeValue(response.getWriter(), body);
    }
}