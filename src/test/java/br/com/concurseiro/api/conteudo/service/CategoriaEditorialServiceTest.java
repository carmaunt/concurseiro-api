package br.com.concurseiro.api.conteudo.service;

import br.com.concurseiro.api.conteudo.dto.CategoriaEditorialRequest;
import br.com.concurseiro.api.conteudo.model.CategoriaEditorial;
import br.com.concurseiro.api.conteudo.model.StatusTaxonomia;
import br.com.concurseiro.api.conteudo.repository.CategoriaEditorialRepository;
import br.com.concurseiro.api.conteudo.repository.ConteudoPortalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaEditorialServiceTest {
    @Mock CategoriaEditorialRepository repository;
    @Mock ConteudoPortalRepository conteudoRepository;

    @Test
    void criarGeraSlugETrataEspacos() {
        var service = new CategoriaEditorialService(repository, conteudoRepository);
        when(repository.save(any())).thenAnswer(invocation -> {
            CategoriaEditorial item = invocation.getArgument(0);
            ReflectionTestUtils.setField(item, "id", 1L);
            return item;
        });
        var response = service.criar(new CategoriaEditorialRequest("  Dicas de Estudo  ", null, " Organização "));
        assertEquals("Dicas de Estudo", response.nome());
        assertEquals("dicas-de-estudo", response.slug());
        assertEquals("Organização", response.descricao());
    }

    @Test
    void criarRecusaSlugDuplicadoIgnorandoCaixa() {
        var service = new CategoriaEditorialService(repository, conteudoRepository);
        when(repository.existsBySlugIgnoreCase("dicas")).thenReturn(true);
        var error = assertThrows(ResponseStatusException.class,
                () -> service.criar(new CategoriaEditorialRequest("Dicas", "DICAS", null)));
        assertEquals(409, error.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    @Test
    void arquivarMantemRegistroESelecaoAtivaNaoIncluiArquivada() {
        var service = new CategoriaEditorialService(repository, conteudoRepository);
        CategoriaEditorial categoria = categoria(1L, "Notícias", StatusTaxonomia.ATIVA);
        when(repository.findById(1L)).thenReturn(Optional.of(categoria));
        when(repository.save(categoria)).thenReturn(categoria);
        service.alterarStatus(1L, StatusTaxonomia.ARQUIVADA);
        assertEquals(StatusTaxonomia.ARQUIVADA, categoria.getStatus());
        when(repository.findAllByStatusOrderByNomeAsc(StatusTaxonomia.ATIVA)).thenReturn(List.of());
        assertTrue(service.listarAtivas().isEmpty());
        verify(repository).findAllByStatusOrderByNomeAsc(StatusTaxonomia.ATIVA);
    }

    private CategoriaEditorial categoria(Long id, String nome, StatusTaxonomia status) {
        CategoriaEditorial item = new CategoriaEditorial();
        ReflectionTestUtils.setField(item, "id", id);
        item.setNome(nome); item.setSlug("noticias"); item.setStatus(status);
        return item;
    }
}
