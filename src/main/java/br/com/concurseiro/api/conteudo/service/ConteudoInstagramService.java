package br.com.concurseiro.api.conteudo.service;

import br.com.concurseiro.api.conteudo.dto.ConteudoPortalResponse;
import br.com.concurseiro.api.conteudo.model.ConteudoPortal;
import br.com.concurseiro.api.conteudo.repository.ConteudoPortalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.OffsetDateTime;

@Service
public class ConteudoInstagramService {
    private final ConteudoPortalRepository repository;
    private final InstagramPublishingClient instagramClient;
    private final String portalPublicUrl;

    public ConteudoInstagramService(
            ConteudoPortalRepository repository,
            InstagramPublishingClient instagramClient,
            @Value("${instagram.portal-public-url:https://appoconcurseiro.com.br}") String portalPublicUrl
    ) {
        this.repository = repository;
        this.instagramClient = instagramClient;
        this.portalPublicUrl = portalPublicUrl.replaceAll("/+$", "");
    }

    @Transactional
    public ConteudoPortalResponse publicar(Long id) {
        ConteudoPortal conteudo = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conteúdo não encontrado"));
        validarProntoParaPublicacao(conteudo);
        if (conteudo.getInstagramMediaId() != null && !conteudo.getInstagramMediaId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este conteúdo já foi publicado no Instagram.");
        }

        try {
            conteudo.setInstagramStatus(ConteudoPortal.InstagramStatus.EM_PROCESSAMENTO);
            conteudo.setInstagramUltimaFalha(null);
            conteudo.setInstagramContainerId(null);
            String containerId = instagramClient.criarContainer(conteudo.getImagemCapa(), montarLegenda(conteudo));
            conteudo.setInstagramContainerId(containerId);
            repository.saveAndFlush(conteudo);

            String mediaId = instagramClient.publicarContainer(containerId);
            conteudo.setInstagramMediaId(mediaId);
            conteudo.setInstagramPublicadoEm(OffsetDateTime.now());
            conteudo.setInstagramStatus(ConteudoPortal.InstagramStatus.PUBLICADO);
            conteudo.setInstagramUltimaFalha(null);
            return ConteudoPortalResponse.fromEntity(repository.save(conteudo));
        } catch (ResponseStatusException ex) {
            conteudo.setInstagramStatus(ConteudoPortal.InstagramStatus.FALHOU);
            conteudo.setInstagramUltimaFalha(limitar(ex.getReason()));
            repository.save(conteudo);
            throw ex;
        }
    }

    private void validarProntoParaPublicacao(ConteudoPortal conteudo) {
        if (conteudo.getStatus() != ConteudoPortal.Status.PUBLICADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Publique o conteúdo no portal antes de enviá-lo ao Instagram.");
        }
        if (conteudo.getImagemCapa() == null || !urlHttps(conteudo.getImagemCapa())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A publicação exige uma imagem de capa com URL pública HTTPS.");
        }
        if (montarLegenda(conteudo).length() > 2200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A legenda do Instagram ultrapassa o limite de 2.200 caracteres.");
        }
    }

    private String montarLegenda(ConteudoPortal conteudo) {
        String corpo = texto(conteudo.getInstagramLegenda());
        if (corpo.isBlank()) corpo = texto(conteudo.getResumo());
        String hashtags = normalizarHashtags(conteudo.getInstagramHashtags());
        String link = portalPublicUrl + caminho(conteudo.getTipo()) + "/" + conteudo.getSlug();
        return String.join("\n\n", texto(conteudo.getTitulo()), corpo, "Leia no portal: " + link, hashtags).trim();
    }

    private String caminho(ConteudoPortal.Tipo tipo) {
        return switch (tipo) {
            case NOTICIA -> "/noticias";
            case BLOG -> "/blog";
            case CONCURSO_ABERTO -> "/concursos-abertos";
            case EDITAL_PREVISTO -> "/editais-previstos";
        };
    }

    private String normalizarHashtags(String value) {
        if (value == null || value.isBlank()) return "";
        return java.util.Arrays.stream(value.split("[\\s,]+"))
                .map(item -> item.trim().replaceFirst("^#", ""))
                .filter(item -> !item.isBlank())
                .map(item -> "#" + item)
                .collect(java.util.stream.Collectors.joining(" "));
    }

    private boolean urlHttps(String value) {
        try { return "https".equalsIgnoreCase(URI.create(value).getScheme()); }
        catch (IllegalArgumentException ignored) { return false; }
    }

    private String texto(String value) { return value == null ? "" : value.trim(); }
    private String limitar(String value) { return value == null ? "Falha ao publicar no Instagram." : value.substring(0, Math.min(500, value.length())); }
}
