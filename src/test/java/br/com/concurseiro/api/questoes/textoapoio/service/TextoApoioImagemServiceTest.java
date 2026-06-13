package br.com.concurseiro.api.questoes.textoapoio.service;

import br.com.concurseiro.api.questoes.textoapoio.dto.TextoApoioRequest;
import br.com.concurseiro.api.questoes.textoapoio.dto.TextoApoioResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TextoApoioImagemServiceTest {

    private R2ImagemStorageService storageService;
    private TextoApoioService textoApoioService;
    private TextoApoioImagemService service;

    @BeforeEach
    void setup() {
        storageService = mock(R2ImagemStorageService.class);
        textoApoioService = mock(TextoApoioService.class);
        service = new TextoApoioImagemService(
                storageService,
                textoApoioService,
                JsonMapper.builder().build()
        );
    }

    @Test
    void cadastrar_deveCriarTextoDeApoioImagem() {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "grafico.png",
                "image/png",
                new byte[]{1}
        );
        R2ImagemStorageService.ImagemArmazenada imagem =
                new R2ImagemStorageService.ImagemArmazenada(
                        "textos-apoio/2026/id.png",
                        "https://assets.example.com/textos-apoio/2026/id.png",
                        "image/png",
                        900,
                        420
                );
        TextoApoioResponse esperado = mock(TextoApoioResponse.class);

        when(storageService.enviar(arquivo)).thenReturn(imagem);
        when(textoApoioService.cadastrar(any())).thenReturn(esperado);

        TextoApoioResponse resultado = service.cadastrar(
                arquivo,
                "Gráfico da questão",
                "Distribuição dos valores observados"
        );

        assertSame(esperado, resultado);
        ArgumentCaptor<TextoApoioRequest> captor = ArgumentCaptor.forClass(TextoApoioRequest.class);
        verify(textoApoioService).cadastrar(captor.capture());

        TextoApoioRequest request = captor.getValue();
        assertEquals("IMAGEM", request.tipo());
        assertEquals("Distribuição dos valores observados", request.conteudo());
        assertTrue(request.conteudoJson().contains("\"largura\":900"));
        assertTrue(request.conteudoJson().contains("\"altura\":420"));
        assertTrue(request.conteudoJson().contains(imagem.url()));
    }

    @Test
    void cadastrar_deveRemoverImagemQuandoCadastroFalhar() {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "grafico.png",
                "image/png",
                new byte[]{1}
        );
        R2ImagemStorageService.ImagemArmazenada imagem =
                new R2ImagemStorageService.ImagemArmazenada(
                        "textos-apoio/2026/id.png",
                        "https://assets.example.com/textos-apoio/2026/id.png",
                        "image/png",
                        900,
                        420
                );

        when(storageService.enviar(arquivo)).thenReturn(imagem);
        when(textoApoioService.cadastrar(any())).thenThrow(new IllegalStateException("falha"));

        assertThrows(
                IllegalStateException.class,
                () -> service.cadastrar(arquivo, null, "Imagem da questão")
        );

        verify(storageService).remover(imagem.chave());
    }

    @Test
    void cadastrar_deveExigirTextoAlternativoAntesDoUpload() {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "grafico.png",
                "image/png",
                new byte[]{1}
        );

        assertThrows(
                ResponseStatusException.class,
                () -> service.cadastrar(arquivo, null, " ")
        );

        verifyNoInteractions(storageService, textoApoioService);
    }
}
