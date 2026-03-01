package br.com.concurseiro.api.usuarios.service;

import br.com.concurseiro.api.usuarios.dto.UsuarioPublicoResponse;
import br.com.concurseiro.api.usuarios.model.Usuario;
import br.com.concurseiro.api.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setNome("Test User");
        usuario.setEmail("test@test.com");
        usuario.setSenhaHash("hashed");
        usuario.setRole(Usuario.Role.VISITANTE);
        usuario.setStatus(Usuario.Status.ATIVO);
    }

    @Test
    void cadastrarVisitante_deveSalvarComSucesso() {
        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario result = service.cadastrarVisitante("Test User", "test@test.com", "senha123");

        verify(repository).save(any(Usuario.class));
        assertEquals(Usuario.Role.VISITANTE, result.getRole());
        assertEquals(Usuario.Status.PENDENTE, result.getStatus());
    }

    @Test
    void cadastrarVisitante_deveFalhar_quandoEmailJaExiste() {
        when(repository.findByEmail("test@test.com")).thenReturn(Optional.of(usuario));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cadastrarVisitante("Test User", "test@test.com", "senha123"));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void autenticar_deveRetornarUsuario_quandoCredenciaisCorretas() {
        when(repository.findByEmail("test@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha123", "hashed")).thenReturn(true);

        Usuario result = service.autenticar("test@test.com", "senha123");

        assertEquals(usuario, result);
    }

    @Test
    void autenticar_deveFalhar_quandoEmailNaoExiste() {
        when(repository.findByEmail("naoexiste@test.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.autenticar("naoexiste@test.com", "senha123"));

        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    void autenticar_deveFalhar_quandoSenhaIncorreta() {
        when(repository.findByEmail("test@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaErrada", "hashed")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.autenticar("test@test.com", "senhaErrada"));

        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    void autenticar_deveFalhar_quandoUsuarioPendente() {
        usuario.setStatus(Usuario.Status.PENDENTE);
        when(repository.findByEmail("test@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha123", "hashed")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.autenticar("test@test.com", "senha123"));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void ativarUsuario_deveAlterarParaAtivo() {
        usuario.setStatus(Usuario.Status.PENDENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario result = service.ativarUsuario(1L);

        assertEquals(Usuario.Status.ATIVO, result.getStatus());
    }

    @Test
    void ativarUsuario_deveFalhar_quandoNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.ativarUsuario(99L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void listarPaginado_deveRetornarPagina() {
        Page<Usuario> page = new PageImpl<>(List.of(usuario));
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        Page<UsuarioPublicoResponse> result = service.listarPaginado(0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("test@test.com", result.getContent().get(0).email());
    }
}
