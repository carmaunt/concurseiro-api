
package br.com.concurseiro.api.infra;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Falha de validação");
        pd.setType(URI.create("https://concurseiro.dev/errors/validation"));
        pd.setInstance(URI.create(req.getRequestURI()));

        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.put(fe.getField(), fe.getDefaultMessage());
        }
        pd.setProperty("fields", fields);

        return pd;
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ProblemDetail handleSpringErrors(ErrorResponseException ex, HttpServletRequest req) {
        ProblemDetail pd = ex.getBody();
        pd.setInstance(URI.create(req.getRequestURI()));
        return pd;
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleBadCredentials(org.springframework.security.authentication.BadCredentialsException ex,
                                              HttpServletRequest req) {

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("Credenciais inválidas");
        pd.setType(URI.create("https://concurseiro.dev/errors/auth"));
        pd.setInstance(URI.create(req.getRequestURI()));
        return pd;
    }

    @ExceptionHandler(org.springframework.security.authentication.DisabledException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ProblemDetail handleDisabled(org.springframework.security.authentication.DisabledException ex,
                                        HttpServletRequest req) {

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Usuário desabilitado");
        pd.setType(URI.create("https://concurseiro.dev/errors/forbidden"));
        pd.setInstance(URI.create(req.getRequestURI()));
        return pd;
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ProblemDetail handleMethodNotAllowed(org.springframework.web.HttpRequestMethodNotSupportedException ex,
                                                HttpServletRequest req) {

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.METHOD_NOT_ALLOWED);
        pd.setTitle("Método não permitido");
        pd.setType(URI.create("https://concurseiro.dev/errors/method-not-allowed"));
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("allowedMethods", ex.getSupportedHttpMethods());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest req) {
        String errorId = UUID.randomUUID().toString();

        // Loga o stacktrace completo no servidor, com um ID para rastrear
        log.error("ERRO_INTERNO id={} method={} uri={} query={}",
                errorId,
                req.getMethod(),
                req.getRequestURI(),
                req.getQueryString(),
                ex
        );

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Erro interno");
        pd.setType(URI.create("https://concurseiro.dev/errors/internal"));
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("errorId", errorId); // devolve o id para você achar no log

        return pd;
    }
}