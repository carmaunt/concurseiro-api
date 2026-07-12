package br.com.concurseiro.api.infra.portal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class PortalRevalidationClient {
    private static final Logger log = LoggerFactory.getLogger(PortalRevalidationClient.class);
    private final HttpClient httpClient;
    private final String url;
    private final String secret;

    @Autowired
    public PortalRevalidationClient(@Value("${portal.revalidate.url:}") String url,
                                    @Value("${portal.revalidate.secret:}") String secret) {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build(), url, secret);
    }

    PortalRevalidationClient(HttpClient httpClient, String url, String secret) {
        this.httpClient = httpClient;
        this.url = url == null ? "" : url.trim();
        this.secret = secret == null ? "" : secret.trim();
    }

    public void revalidar() {
        if (url.isBlank() || secret.isBlank()) {
            log.warn("Revalidação do portal ignorada: configuração ausente");
            return;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .header("Authorization", "Bearer " + secret)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            int status = httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
            if (status >= 200 && status < 300) log.info("Revalidação do portal concluída");
            else log.warn("Revalidação do portal respondeu com status {}", status);
        } catch (Exception ex) {
            log.warn("Falha ao revalidar o portal: {}", ex.getClass().getSimpleName());
        }
    }
}
