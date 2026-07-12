package br.com.concurseiro.api.infra.portal;

import org.junit.jupiter.api.Test;
import java.net.http.HttpClient;
import static org.mockito.Mockito.*;

class PortalRevalidationClientTest {
    @Test
    void configuracaoAusenteNaoChamaHttp() {
        HttpClient client = mock(HttpClient.class);
        new PortalRevalidationClient(client, "", "").revalidar();
        verifyNoInteractions(client);
    }

    @Test
    void falhaDoPortalNaoPropagaExcecao() throws Exception {
        HttpClient client = mock(HttpClient.class);
        when(client.send(any(), any())).thenThrow(new java.io.IOException("indisponível"));
        new PortalRevalidationClient(client, "https://portal.test/api/revalidate", "segredo").revalidar();
        verify(client).send(any(), any());
    }
}
