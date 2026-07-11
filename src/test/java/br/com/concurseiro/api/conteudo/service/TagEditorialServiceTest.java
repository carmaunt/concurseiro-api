package br.com.concurseiro.api.conteudo.service;

import br.com.concurseiro.api.conteudo.dto.TagEditorialRequest;
import br.com.concurseiro.api.conteudo.model.TagEditorial;
import br.com.concurseiro.api.conteudo.repository.ConteudoPortalRepository;
import br.com.concurseiro.api.conteudo.repository.TagEditorialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagEditorialServiceTest {
    @Mock TagEditorialRepository repository;
    @Mock ConteudoPortalRepository conteudoRepository;

    @Test
    void criarTagGeraSlug() {
        var service = new TagEditorialService(repository, conteudoRepository);
        when(repository.save(any())).thenAnswer(invocation -> {
            TagEditorial item = invocation.getArgument(0);
            ReflectionTestUtils.setField(item, "id", 1L);
            return item;
        });
        var response = service.criar(new TagEditorialRequest("Polícia Civil", null));
        assertEquals("policia-civil", response.slug());
    }

    @Test
    void criarTagRecusaNomeDuplicado() {
        var service = new TagEditorialService(repository, conteudoRepository);
        when(repository.existsByNomeIgnoreCase("Revisão")).thenReturn(true);
        var error = assertThrows(ResponseStatusException.class,
                () -> service.criar(new TagEditorialRequest("Revisão", null)));
        assertEquals(409, error.getStatusCode().value());
        verify(repository, never()).save(any());
    }
}
