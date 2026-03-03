package br.com.concurseiro.api.catalogo.disciplina.service;

import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import br.com.concurseiro.api.catalogo.disciplina.model.Disciplina;
import br.com.concurseiro.api.catalogo.disciplina.repository.DisciplinaRepository;
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
class DisciplinaServiceTest {

    @Mock
    private DisciplinaRepository repository;

    @InjectMocks
    private DisciplinaService service;

    private Disciplina disciplina;

    @BeforeEach
    void setUp() throws Exception {
        disciplina = new Disciplina();
        disciplina.setNome("Matemática");
        var f = Disciplina.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(disciplina, 1L);
    }

    @Test
    void cadastrar_deveSalvar_quandoNomeNovo() {
        when(repository.existsByNomeIgnoreCase("Matemática")).thenReturn(false);
        when(repository.save(any(Disciplina.class))).thenReturn(disciplina);

        CatalogoItemResponse response = service.cadastrar(" Matemática ");

        assertEquals(1L, response.id());
        assertEquals("Matemática", response.nome());
        verify(repository).save(any(Disciplina.class));
    }

    @Test
    void cadastrar_deveFalhar_quandoNomeDuplicado() {
        when(repository.existsByNomeIgnoreCase("Matemática")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cadastrar("Matemática"));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void listar_deveRetornarLista() {
        when(repository.findAllByOrderByNomeAsc()).thenReturn(List.of(disciplina));

        List<CatalogoItemResponse> result = service.listar();

        assertEquals(1, result.size());
        assertEquals("Matemática", result.get(0).nome());
    }

    @Test
    void atualizar_deveAtualizarNome() {
        when(repository.findById(1L)).thenReturn(Optional.of(disciplina));
        when(repository.existsByNomeIgnoreCase("Português")).thenReturn(false);
        Disciplina atualizada = new Disciplina();
        atualizada.setNome("Português");
        try {
            var f = Disciplina.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(atualizada, 1L);
        } catch (Exception e) { throw new RuntimeException(e); }
        when(repository.save(any(Disciplina.class))).thenReturn(atualizada);

        CatalogoItemResponse response = service.atualizar(1L, " Português ");

        assertEquals("Português", response.nome());
        verify(repository).save(any(Disciplina.class));
    }

    @Test
    void atualizar_deveFalhar_quandoNaoEncontrado() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizar(1L, "Português"));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void atualizar_deveFalhar_quandoNomeDuplicadoDeOutroRegistro() {
        when(repository.findById(1L)).thenReturn(Optional.of(disciplina));
        when(repository.existsByNomeIgnoreCase("Português")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizar(1L, "Português"));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void excluir_deveDeletar_quandoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(disciplina));

        service.excluir(1L);

        verify(repository).delete(disciplina);
    }

    @Test
    void excluir_deveFalhar_quandoNaoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.excluir(1L));
        assertEquals(404, ex.getStatusCode().value());
    }
}
