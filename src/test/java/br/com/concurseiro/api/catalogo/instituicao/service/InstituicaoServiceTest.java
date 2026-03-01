package br.com.concurseiro.api.catalogo.instituicao.service;

import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import br.com.concurseiro.api.catalogo.instituicao.model.Instituicao;
import br.com.concurseiro.api.catalogo.instituicao.repository.InstituicaoRepository;
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
class InstituicaoServiceTest {

    @Mock
    private InstituicaoRepository repository;

    @InjectMocks
    private InstituicaoService service;

    private Instituicao instituicao;

    @BeforeEach
    void setUp() throws Exception {
        instituicao = new Instituicao();
        instituicao.setNome("INSS");
        var f = Instituicao.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(instituicao, 1L);
    }

    @Test
    void cadastrar_deveSalvar_quandoNomeNovo() {
        when(repository.existsByNomeIgnoreCase("INSS")).thenReturn(false);
        when(repository.save(any(Instituicao.class))).thenReturn(instituicao);

        CatalogoItemResponse response = service.cadastrar(" INSS ");

        assertEquals(1L, response.id());
        assertEquals("INSS", response.nome());
        verify(repository).save(any(Instituicao.class));
    }

    @Test
    void cadastrar_deveFalhar_quandoNomeDuplicado() {
        when(repository.existsByNomeIgnoreCase("INSS")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cadastrar("INSS"));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void listar_deveRetornarLista() {
        when(repository.findAllByOrderByNomeAsc()).thenReturn(List.of(instituicao));

        List<CatalogoItemResponse> result = service.listar();

        assertEquals(1, result.size());
        assertEquals("INSS", result.get(0).nome());
    }

    @Test
    void atualizar_deveAtualizarNome() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(instituicao));
        when(repository.existsByNomeIgnoreCase("IBGE")).thenReturn(false);
        Instituicao atualizada = new Instituicao();
        atualizada.setNome("IBGE");
        var f = Instituicao.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(atualizada, 1L);
        when(repository.save(any(Instituicao.class))).thenReturn(atualizada);

        CatalogoItemResponse response = service.atualizar(1L, " IBGE ");

        assertEquals("IBGE", response.nome());
        verify(repository).save(any(Instituicao.class));
    }

    @Test
    void atualizar_deveFalhar_quandoNaoEncontrado() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizar(1L, "IBGE"));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void atualizar_deveFalhar_quandoNomeDuplicadoDeOutroRegistro() {
        when(repository.findById(1L)).thenReturn(Optional.of(instituicao));
        when(repository.existsByNomeIgnoreCase("IBGE")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizar(1L, "IBGE"));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void excluir_deveDeletar_quandoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(instituicao));

        service.excluir(1L);

        verify(repository).delete(instituicao);
    }

    @Test
    void excluir_deveFalhar_quandoNaoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.excluir(1L));
        assertEquals(404, ex.getStatusCode().value());
    }
}
