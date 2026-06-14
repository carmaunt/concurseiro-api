package br.com.concurseiro.api.questoes.enunciado.service;

import br.com.concurseiro.api.questoes.enunciado.model.Enunciado;
import br.com.concurseiro.api.questoes.enunciado.repository.EnunciadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EnunciadoServiceTest {

    private EnunciadoRepository repository;
    private EnunciadoService service;

    @BeforeEach
    void setup() {
        repository = mock(EnunciadoRepository.class);
        service = new EnunciadoService(repository);
    }

    @Test
    void resolverEnunciado_deveReutilizarRegistroComMesmoConteudo() {
        Enunciado existente = enunciado(7L, "Julgue o item subsequente.");
        when(repository.findByHashSha256(
                "51fdd0254593544423f3b200203fed52e817cb03b57b317144df0cdbdcc19807"
        )).thenReturn(Optional.of(existente));

        Enunciado resultado = service.resolverEnunciado(null, "  Julgue o item subsequente.  ");

        assertSame(existente, resultado);
        verify(repository, never()).save(any());
    }

    @Test
    void resolverEnunciado_deveCriarRegistroQuandoConteudoForNovo() {
        Enunciado persistido = enunciado(8L, "Julgue o item.");
        when(repository.findByHashSha256(anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(persistido));

        Enunciado resultado = service.resolverEnunciado(null, "  Julgue o item.  ");

        assertEquals("Julgue o item.", resultado.getConteudo());
        verify(repository).inserirSeAusente(
                "Julgue o item.",
                "65db16ade657d54516bcd4e8a4913138a69396864b1e15493f00beb8cc1b1a56"
        );
    }

    @Test
    void resolverEnunciado_devePriorizarIdQuandoInformado() {
        Enunciado existente = enunciado(12L, "Com base no texto, julgue o item.");
        when(repository.findById(12L)).thenReturn(Optional.of(existente));

        Enunciado resultado = service.resolverEnunciado(12L, "Texto ignorado");

        assertSame(existente, resultado);
        verify(repository, never()).findByHashSha256(any());
        verify(repository, never()).save(any());
    }

    @Test
    void resolverEnunciado_deveFalharQuandoIdNaoExistir() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.resolverEnunciado(99L, null)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    private Enunciado enunciado(Long id, String conteudo) {
        Enunciado enunciado = new Enunciado();
        enunciado.setId(id);
        enunciado.setConteudo(conteudo);
        enunciado.setHashSha256("hash");
        return enunciado;
    }
}
