package br.com.concurseiro.api.conteudo.service;

import br.com.concurseiro.api.conteudo.dto.ConteudoPortalRequest;
import br.com.concurseiro.api.conteudo.model.*;
import br.com.concurseiro.api.conteudo.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConteudoPortalTaxonomiaServiceTest {
    @Mock ConteudoPortalRepository repository;
    @Mock CategoriaEditorialRepository categoriaRepository;
    @Mock TagEditorialRepository tagRepository;

    @Test
    void criarAssociaCategoriaEMultiplasTagsERetornaContratoPublico() {
        var categoria = categoria(10L, "Concursos", StatusTaxonomia.ATIVA);
        var tag1 = tag(20L, "Edital", StatusTaxonomia.ATIVA);
        var tag2 = tag(21L, "Polícia", StatusTaxonomia.ATIVA);
        when(categoriaRepository.findById(10L)).thenReturn(java.util.Optional.of(categoria));
        when(tagRepository.findAllByIdIn(Set.of(20L, 21L))).thenReturn(List.of(tag1, tag2));
        when(repository.save(any())).thenAnswer(invocation -> {
            ConteudoPortal item = invocation.getArgument(0);
            ReflectionTestUtils.setField(item, "id", 1L);
            return item;
        });
        var response = service().criar(request(10L, Set.of(20L, 21L)));
        assertEquals("Concursos", response.category().nome());
        assertEquals("Concursos", response.categoria());
        assertEquals(2, response.tags().size());
        assertEquals(Set.of("Edital", "Polícia"), response.tags().stream().map(item -> item.nome()).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void criarRecusaCategoriaArquivada() {
        when(categoriaRepository.findById(10L)).thenReturn(java.util.Optional.of(categoria(10L, "Antiga", StatusTaxonomia.ARQUIVADA)));
        var error = assertThrows(ResponseStatusException.class, () -> service().criar(request(10L, Set.of())));
        assertEquals(400, error.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    @Test
    void criarRecusaTagArquivada() {
        var tag = tag(20L, "Antiga", StatusTaxonomia.ARQUIVADA);
        when(tagRepository.findAllByIdIn(Set.of(20L))).thenReturn(List.of(tag));
        var error = assertThrows(ResponseStatusException.class, () -> service().criar(request(null, Set.of(20L))));
        assertEquals(400, error.getStatusCode().value());
    }

    private ConteudoPortalService service() { return new ConteudoPortalService(repository, categoriaRepository, tagRepository); }

    private ConteudoPortalRequest request(Long categoriaId, Set<Long> tagIds) {
        return new ConteudoPortalRequest("Título", "titulo", "Resumo", "Conteúdo", null, categoriaId, tagIds,
                ConteudoPortal.Status.RASCUNHO, ConteudoPortal.Tipo.NOTICIA, false, null, null);
    }

    private CategoriaEditorial categoria(Long id, String nome, StatusTaxonomia status) {
        var item = new CategoriaEditorial(); ReflectionTestUtils.setField(item, "id", id);
        item.setNome(nome); item.setSlug(ConteudoPortal.gerarSlug(nome)); item.setStatus(status); return item;
    }

    private TagEditorial tag(Long id, String nome, StatusTaxonomia status) {
        var item = new TagEditorial(); ReflectionTestUtils.setField(item, "id", id);
        item.setNome(nome); item.setSlug(ConteudoPortal.gerarSlug(nome)); item.setStatus(status); return item;
    }
}
