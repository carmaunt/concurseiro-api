package br.com.concurseiro.api.infra.response;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.*;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ApiResponseEnvelopeAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // decide no beforeBodyWrite
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                 MethodParameter returnType,
                                 MediaType selectedContentType,
                                 Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                 ServerHttpRequest request,
                                 ServerHttpResponse response) {

        // Só padroniza a API versionada
        String path = null;
        if (request instanceof ServletServerHttpRequest sr) {
            HttpServletRequest servletReq = sr.getServletRequest();
            path = servletReq.getRequestURI();
        }
        if (path == null || !path.startsWith("/api/v1/")) return body;

        // Não embrulha erros nem respostas já embrulhadas no padrão oficial
        if (body == null) return ApiResponse.success(null, path);
        if (body instanceof ProblemDetail) return body;
        if (body instanceof br.com.concurseiro.api.infra.response.ApiResponse<?>) return body;

        // Embrulha qualquer resposta "crua" no padrão {success,data,timestamp,path}
        return ApiResponse.success(body, path);
    }
}