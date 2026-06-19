package br.com.concurseiro.api.analytics.service;

import br.com.concurseiro.api.analytics.dto.AnalyticsEventRequest;
import br.com.concurseiro.api.analytics.model.AppEvent;
import br.com.concurseiro.api.analytics.repository.AnalyticsQueryRepository;
import br.com.concurseiro.api.analytics.repository.AppEventRepository;
import br.com.concurseiro.api.catalogo.assunto.repository.AssuntoRepository;
import br.com.concurseiro.api.catalogo.disciplina.repository.DisciplinaRepository;
import br.com.concurseiro.api.catalogo.subassunto.repository.SubAssuntoRepository;
import br.com.concurseiro.api.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock AppEventRepository events;
    @Mock AnalyticsQueryRepository queries;
    @Mock UsuarioRepository usuarios;
    @Mock DisciplinaRepository disciplinas;
    @Mock AssuntoRepository assuntos;
    @Mock SubAssuntoRepository subassuntos;

    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(
                events, queries, usuarios, disciplinas, assuntos, subassuntos
        );
    }

    @Test
    void deveRegistrarEventoAnonimoComDeviceId() {
        when(events.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        AnalyticsEventRequest request = request("screen_view", "device-opaque", Map.of("source", "home"));

        service.register(request, null);

        var captor = org.mockito.ArgumentCaptor.forClass(AppEvent.class);
        verify(events).save(captor.capture());
        assertEquals("screen_view", captor.getValue().getEventName());
        assertEquals("device-opaque", captor.getValue().getDeviceId());
        assertEquals("home", captor.getValue().getMetadata().get("source").asText());
        assertNull(captor.getValue().getUsuario());
    }

    @Test
    void deveExigirDeviceIdParaEventoAnonimo() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.register(request("app_open", null, Map.of()), null));

        assertEquals(400, error.getStatusCode().value());
        verify(events, never()).save(any());
    }

    @Test
    void deveRejeitarMetadadoSensivel() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.register(request("app_error", "device-1", Map.of("userEmail", "x@y.com")), null));

        assertEquals(400, error.getStatusCode().value());
        verify(events, never()).save(any());
    }

    private AnalyticsEventRequest request(String eventName, String deviceId, Map<String, Object> metadata) {
        return new AnalyticsEventRequest(
                eventName, deviceId, "session-1", "home", null, null, null,
                null, null, null, null, "1.0.0", "android", metadata
        );
    }
}
