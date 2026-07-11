package br.com.concurseiro.api.estudante.controller;

import br.com.concurseiro.api.estudante.dto.DashboardEstudanteResponse;
import br.com.concurseiro.api.estudante.service.DashboardEstudanteService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/estudante")
public class DashboardEstudanteController {

    private final DashboardEstudanteService service;

    public DashboardEstudanteController(DashboardEstudanteService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public DashboardEstudanteResponse dashboard(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        return service.carregar(authentication.getName());
    }
}
