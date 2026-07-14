package br.com.concurseiro.api.conteudo.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InstagramPublishingClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String apiBaseUrl;
    private final String accountId;
    private final String accessToken;

    public InstagramPublishingClient(
            ObjectMapper objectMapper,
            @Value("${instagram.enabled:false}") boolean enabled,
            @Value("${instagram.api-base-url:https://graph.instagram.com}") String apiBaseUrl,
            @Value("${instagram.account-id:}") String accountId,
            @Value("${instagram.access-token:}") String accessToken
    ) {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.apiBaseUrl = apiBaseUrl.replaceAll("/+$", "");
        this.accountId = accountId == null ? "" : accountId.trim();
        this.accessToken = accessToken == null ? "" : accessToken.trim();
    }

    public String criarContainer(String imagemUrl, String legenda) {
        validarConfiguracao();
        return enviar("/" + accountId + "/media", Map.of("image_url", imagemUrl, "caption", legenda));
    }

    public String publicarContainer(String containerId) {
        validarConfiguracao();
        aguardarContainerPronto(containerId);
        return enviar("/" + accountId + "/media_publish", Map.of("creation_id", containerId));
    }

    private void aguardarContainerPronto(String containerId) {
        for (int tentativa = 0; tentativa < 12; tentativa++) {
            String status = consultarStatusContainer(containerId);
            if ("FINISHED".equalsIgnoreCase(status)) return;

            if ("ERROR".equalsIgnoreCase(status) || "EXPIRED".equalsIgnoreCase(status)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O Instagram não conseguiu processar a imagem. Envie outra capa e tente novamente.");
            }

            try {
                Thread.sleep(2_000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "A publicação no Instagram foi interrompida. Tente novamente.");
            }
        }

        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "O Instagram ainda está processando a imagem. Aguarde alguns segundos e tente novamente.");
    }

    private String consultarStatusContainer(String containerId) {
        try {
            String query = "fields=status_code&access_token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(apiBaseUrl + "/" + containerId + "?" + query))
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());
            if (response.statusCode() < 200 || response.statusCode() >= 300) throw falhaResposta(response.statusCode(), json);
            return json.path("status_code").asText();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Não foi possível verificar o processamento da imagem no Instagram.");
        }
    }

    private String enviar(String path, Map<String, String> parametros) {
        try {
            String body = java.util.stream.Stream.concat(
                            parametros.entrySet().stream().map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)),
                            java.util.stream.Stream.of("access_token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8))
                    )
                    .collect(Collectors.joining("&"));
            HttpRequest request = HttpRequest.newBuilder(URI.create(apiBaseUrl + path))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());
            if (response.statusCode() < 200 || response.statusCode() >= 300 || json.path("id").asText().isBlank()) {
                throw falhaResposta(response.statusCode(), json);
            }
            return json.path("id").asText();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Não foi possível comunicar com o Instagram. Tente novamente.");
        }
    }

    private ResponseStatusException falhaResposta(int status, JsonNode json) {
        String message = json.path("error").path("message").asText();
        String safeMessage = message == null || message.isBlank()
                ? "O Instagram recusou a publicação. Verifique a conta e tente novamente."
                : "O Instagram recusou a publicação: " + message.replaceAll("[\\r\\n]+", " ");
        return new ResponseStatusException(status == 401 || status == 403 ? HttpStatus.BAD_GATEWAY : HttpStatus.BAD_REQUEST, safeMessage);
    }

    private void validarConfiguracao() {
        if (!enabled || accountId.isBlank() || accessToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "A publicação no Instagram ainda não está configurada.");
        }
    }
}
