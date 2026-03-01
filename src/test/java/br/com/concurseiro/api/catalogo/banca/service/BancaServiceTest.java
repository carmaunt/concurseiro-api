package br.com.concurseiro.api.catalogo.banca.service;

import br.com.concurseiro.api.catalogo.banca.model.Banca;
import br.com.concurseiro.api.catalogo.banca.repository.BancaRepository;
import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BancaServiceTest {

    @Mock
    private BancaRepository repository;

    @InjectMocks
    private BancaService service;

    private Banca banca;

    @BeforeEach
    void setUp() throws Exception {
        banca = new Banca();
        banca.setNome("CESPE");
        var f = Banca.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(banca, 1L);
    }

    @Test
    void cadastrar_deveSalvar_quandoNomeNovo() {
        when(repository.existsByNomeIgnoreCase("CESPE")).thenReturn(false);
        when(repository.save(any(Banca.class))).thenReturn(banca);

        CatalogoItemResponse response = service.cadastrar(" CESPE ");

        assertEquals(1L, response.id());
        assertEquals("CESPE", response.nome());
        verify(repository).save(any(Banca.class));
    }

    @Test
    void cadastrar_deveFalhar_quandoNomeDuplicado() {
        when(repository.existsByNomeIgnoreCase("CESPE")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cadastrar("CESPE"));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void listar_deveRetornarLista() {
        when(repository.findAllByOrderByNomeAsc()).thenReturn(List.of(banca));

        List<CatalogoItemResponse> result = service.listar();

        assertEquals(1, result.size());
        assertEquals("CESPE", result.get(0).nome());
    }

    @Test
    void atualizar_deveAtualizarNome() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(banca));
        when(repository.existsByNomeIgnoreCase("FCC")).thenReturn(false);
        Banca atualizada = new Banca();
        atualizada.setNome("FCC");
        var f = Banca.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(atualizada, 1L);
        when(repository.save(any(Banca.class))).thenReturn(atualizada);

        CatalogoItemResponse response = service.atualizar(1L, " FCC ");

        assertEquals("FCC", response.nome());
        verify(repository).save(any(Banca.class));
    }

    @Test
    void atualizar_deveFalhar_quandoNaoEncontrado() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizar(1L, "FCC"));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void atualizar_deveFalhar_quandoNomeDuplicadoDeOutroRegistro() {
        when(repository.findById(1L)).thenReturn(Optional.of(banca));
        when(repository.existsByNomeIgnoreCase("FCC")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizar(1L, "FCC"));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void excluir_deveDeletar_quandoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(banca));

        service.excluir(1L);

        verify(repository).delete(banca);
    }

    @Test
    void excluir_deveFalhar_quandoNaoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.excluir(1L));
        assertEquals(404, ex.getStatusCode().value());
    }
}
