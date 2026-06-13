package br.com.concurseiro.api.questoes.textoapoio.service;

import br.com.concurseiro.api.infra.storage.R2ConfiguredCondition;
import br.com.concurseiro.api.questoes.textoapoio.dto.TextoApoioRequest;
import br.com.concurseiro.api.questoes.textoapoio.dto.TextoApoioResponse;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
@Conditional(R2ConfiguredCondition.class)
public class TextoApoioImagemService {

    private static final int TAMANHO_MAXIMO_TEXTO_ALTERNATIVO = 500;

    private final R2ImagemStorageService storageService;
    private final TextoApoioService textoApoioService;
    private final ObjectMapper objectMapper;

    public TextoApoioImagemService(
            R2ImagemStorageService storageService,
            TextoApoioService textoApoioService,
            ObjectMapper objectMapper
    ) {
        this.storageService = storageService;
        this.textoApoioService = textoApoioService;
        this.objectMapper = objectMapper;
    }

    public TextoApoioResponse cadastrar(
            MultipartFile arquivo,
            String titulo,
            String textoAlternativo
    ) {
        String alt = normalizarTextoAlternativo(textoAlternativo);
        R2ImagemStorageService.ImagemArmazenada imagem = storageService.enviar(arquivo);

        try {
            String conteudoJson = criarConteudoJson(imagem, alt);
            return textoApoioService.cadastrar(
                    new TextoApoioRequest(titulo, "IMAGEM", alt, conteudoJson)
            );
        } catch (RuntimeException ex) {
            storageService.remover(imagem.chave());
            throw ex;
        }
    }

    private String criarConteudoJson(
            R2ImagemStorageService.ImagemArmazenada imagem,
            String textoAlternativo
    ) {
        ObjectNode json = objectMapper.createObjectNode();
        json.put("url", imagem.url());
        json.put("alt", textoAlternativo);
        json.put("mimeType", imagem.contentType());

        if (imagem.largura() != null) {
            json.put("largura", imagem.largura());
        }
        if (imagem.altura() != null) {
            json.put("altura", imagem.altura());
        }

        try {
            return objectMapper.writeValueAsString(json);
        } catch (JacksonException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "não foi possível preparar os dados da imagem",
                    ex
            );
        }
    }

    private String normalizarTextoAlternativo(String textoAlternativo) {
        if (textoAlternativo == null || textoAlternativo.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "texto alternativo da imagem é obrigatório"
            );
        }

        String normalizado = textoAlternativo.trim().replaceAll("\\s+", " ");
        if (normalizado.length() > TAMANHO_MAXIMO_TEXTO_ALTERNATIVO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "texto alternativo deve ter no máximo " + TAMANHO_MAXIMO_TEXTO_ALTERNATIVO + " caracteres"
            );
        }
        return normalizado;
    }
}
