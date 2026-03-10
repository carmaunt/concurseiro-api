package br.com.concurseiro.api.infra.config;

import br.com.concurseiro.api.infra.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

import br.com.concurseiro.api.usuarios.model.Usuario;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final br.com.concurseiro.api.infra.security.RateLimitFilter rateLimitFilter;

    @Value("${cors.allowed-origins}")
    private String corsAllowedOrigins;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, br.com.concurseiro.api.infra.security.RateLimitFilter rateLimitFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((req, res, authEx) -> {
                        res.setStatus(HttpStatus.UNAUTHORIZED.value());
                        res.setContentType("application/problem+json");
                        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
                        pd.setTitle("Não autenticado");
                        pd.setType(URI.create("https://concurseiro.dev/errors/auth"));
                        pd.setInstance(URI.create(req.getRequestURI()));
                        new com.fasterxml.jackson.databind.ObjectMapper().writeValue(res.getOutputStream(), pd);
                    })
                    .accessDeniedHandler((req, res, deniedEx) -> {
                        res.setStatus(HttpStatus.FORBIDDEN.value());
                        res.setContentType("application/problem+json");
                        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
                        pd.setTitle("Acesso negado");
                        pd.setType(URI.create("https://concurseiro.dev/errors/forbidden"));
                        pd.setInstance(URI.create(req.getRequestURI()));
                        new com.fasterxml.jackson.databind.ObjectMapper().writeValue(res.getOutputStream(), pd);
                    })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // leitura pública
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/questoes/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/catalogo/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/provas/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/questoes/*/comentarios").permitAll()

                        // ações permitidas para qualquer usuário autenticado e ativo
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/questoes")
                            .hasAnyAuthority(Usuario.Role.ADMIN.authority(), Usuario.Role.VISITANTE.authority())
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/questoes/*/comentarios")
                            .hasAnyAuthority(Usuario.Role.ADMIN.authority(), Usuario.Role.VISITANTE.authority())
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/provas")
                            .hasAnyAuthority(Usuario.Role.ADMIN.authority(), Usuario.Role.VISITANTE.authority())
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/provas/*/questoes")
                            .hasAnyAuthority(Usuario.Role.ADMIN.authority(), Usuario.Role.VISITANTE.authority())
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/comentarios/*/curtir")
                            .hasAnyAuthority(Usuario.Role.ADMIN.authority(), Usuario.Role.VISITANTE.authority())
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/comentarios/*/descurtir")
                            .hasAnyAuthority(Usuario.Role.ADMIN.authority(), Usuario.Role.VISITANTE.authority())

                        // ações exclusivas de admin
                        .requestMatchers("/api/v1/admin/**").hasAuthority(Usuario.Role.ADMIN.authority())

                        .anyRequest().authenticated()
                )
                .addFilterBefore(rateLimitFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        List<String> origins = Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        boolean isWildcard = origins.size() == 1 && origins.get(0).equals("*");

        if (isWildcard) {
            cfg.setAllowedOriginPatterns(List.of("*"));
            cfg.setAllowCredentials(false);
        } else {
            cfg.setAllowedOrigins(origins);
            cfg.setAllowCredentials(true);
        }

        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}