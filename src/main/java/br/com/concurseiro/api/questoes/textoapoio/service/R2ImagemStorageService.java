package br.com.concurseiro.api.questoes.textoapoio.service;

import br.com.concurseiro.api.infra.storage.R2ConfiguredCondition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Year;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Conditional(R2ConfiguredCondition.class)
public class R2ImagemStorageService {

    private static final long TAMANHO_MAXIMO = 5L * 1024 * 1024;
    private static final String PREFIXO_TEXTOS_APOIO = "textos-apoio";
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp"
    );
    private static final Map<String, String> EXTENSOES = Map.of(
            "image/png", "png",
            "image/jpeg", "jpg",
            "image/webp", "webp"
    );

    private final S3Client s3Client;
    private final String bucket;
    private final String publicBaseUrl;

    public R2ImagemStorageService(
            S3Client s3Client,
            @Value("${r2.bucket}") String bucket,
            @Value("${r2.public-base-url}") String publicBaseUrl
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.publicBaseUrl = removerBarraFinal(publicBaseUrl);
    }

    public ImagemArmazenada enviar(MultipartFile arquivo) {
        return enviar(arquivo, PREFIXO_TEXTOS_APOIO);
    }

    public ImagemArmazenada enviar(MultipartFile arquivo, String prefixo) {
        validarArquivo(arquivo);

        String contentType = arquivo.getContentType().toLowerCase(Locale.ROOT);
        byte[] bytes = lerBytes(arquivo);
        Dimensoes dimensoes = lerDimensoes(bytes);
        String prefixoNormalizado = normalizarPrefixo(prefixo);
        String chave = prefixoNormalizado + "/" + Year.now().getValue() + "/" +
                UUID.randomUUID() + "." + EXTENSOES.get(contentType);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(chave)
                .contentType(contentType)
                .cacheControl("public, max-age=31536000, immutable")
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(bytes));
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "não foi possível enviar a imagem para o armazenamento",
                    ex
            );
        }

        return new ImagemArmazenada(
                chave,
                publicBaseUrl + "/" + chave,
                contentType,
                dimensoes.largura(),
                dimensoes.altura()
        );
    }

    public void remover(String chave) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(chave)
                    .build());
        } catch (RuntimeException ignored) {
            // A falha de limpeza não deve ocultar o erro original do cadastro.
        }
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "arquivo de imagem é obrigatório");
        }

        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "a imagem deve ter no máximo 5 MB");
        }

        String contentType = arquivo.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "formato de imagem inválido; use PNG, JPEG ou WebP"
            );
        }
    }

    private byte[] lerBytes(MultipartFile arquivo) {
        try {
            return arquivo.getBytes();
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "não foi possível ler a imagem", ex);
        }
    }

    private Dimensoes lerDimensoes(byte[] bytes) {
        try {
            BufferedImage imagem = ImageIO.read(new ByteArrayInputStream(bytes));
            if (imagem == null) {
                return new Dimensoes(null, null);
            }
            return new Dimensoes(imagem.getWidth(), imagem.getHeight());
        } catch (IOException ex) {
            return new Dimensoes(null, null);
        }
    }

    private static String removerBarraFinal(String valor) {
        return valor == null ? "" : valor.replaceAll("/+$", "");
    }

    private static String normalizarPrefixo(String valor) {
        if (valor == null || valor.isBlank()) {
            return PREFIXO_TEXTOS_APOIO;
        }

        String normalizado = valor.trim().replaceAll("^/+|/+$", "");
        return normalizado.isBlank() ? PREFIXO_TEXTOS_APOIO : normalizado;
    }

    public record ImagemArmazenada(
            String chave,
            String url,
            String contentType,
            Integer largura,
            Integer altura
    ) {
    }

    private record Dimensoes(Integer largura, Integer altura) {
    }
}
