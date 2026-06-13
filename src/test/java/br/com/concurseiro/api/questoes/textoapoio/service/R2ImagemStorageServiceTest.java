package br.com.concurseiro.api.questoes.textoapoio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class R2ImagemStorageServiceTest {

    private S3Client s3Client;
    private R2ImagemStorageService service;

    @BeforeEach
    void setup() {
        s3Client = mock(S3Client.class);
        service = new R2ImagemStorageService(
                s3Client,
                "oconcurseiro-assets",
                "https://assets.example.com/"
        );
    }

    @Test
    void enviar_devePublicarPngComMetadados() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "grafico.png",
                "image/png",
                criarPng(320, 180)
        );
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        R2ImagemStorageService.ImagemArmazenada resultado = service.enviar(arquivo);

        assertEquals("image/png", resultado.contentType());
        assertEquals(320, resultado.largura());
        assertEquals(180, resultado.altura());
        assertTrue(resultado.chave().matches("textos-apoio/\\d{4}/[\\w-]+\\.png"));
        assertEquals("https://assets.example.com/" + resultado.chave(), resultado.url());

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("oconcurseiro-assets", captor.getValue().bucket());
        assertEquals("image/png", captor.getValue().contentType());
        assertEquals("public, max-age=31536000, immutable", captor.getValue().cacheControl());
    }

    @Test
    void enviar_deveRecusarArquivoQueNaoSejaImagem() {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "prova.txt",
                "text/plain",
                "conteudo".getBytes()
        );

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.enviar(arquivo)
        );

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, erro.getStatusCode());
        verifyNoInteractions(s3Client);
    }

    @Test
    void enviar_deveRecusarImagemMaiorQueCincoMegabytes() {
        byte[] conteudo = new byte[5 * 1024 * 1024 + 1];
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "grande.png",
                "image/png",
                conteudo
        );

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.enviar(arquivo)
        );

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, erro.getStatusCode());
        verifyNoInteractions(s3Client);
    }

    private byte[] criarPng(int largura, int altura) throws Exception {
        BufferedImage imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(imagem, "png", output);
        return output.toByteArray();
    }
}
