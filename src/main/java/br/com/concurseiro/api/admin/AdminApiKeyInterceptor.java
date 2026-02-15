// src/main/java/br/com/concurseiro/api/admin/AdminApiKeyInterceptor.java
package br.com.concurseiro.api.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminApiKeyInterceptor implements HandlerInterceptor {

    @Value("${app.admin.api-key}")
    private String apiKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String providedKey = request.getHeader("X-API-KEY");
        if (providedKey == null || !providedKey.equals(apiKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chave inválida");
        }
        return true;
    }
}