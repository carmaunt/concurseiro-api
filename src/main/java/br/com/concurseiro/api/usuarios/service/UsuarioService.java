package br.com.concurseiro.api.usuarios.service;

import br.com.concurseiro.api.usuarios.dto.UsuarioPublicoResponse;
import br.com.concurseiro.api.usuarios.model.Usuario;
import br.com.concurseiro.api.usuarios.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario cadastrarVisitante(String nome, String email, String senha) {

        if (repository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email já cadastrado"
            );
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setRole(Usuario.Role.VISITANTE);
        usuario.setStatus(Usuario.Status.PENDENTE);

        return repository.save(usuario);
    }

    @Transactional
    public Usuario ativarUsuario(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado")
                );

        usuario.setStatus(Usuario.Status.ATIVO);
        return repository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioPublicoResponse> listarPaginado(int page, int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("criadoEm").descending());
        return repository.findAll(pageable).map(UsuarioPublicoResponse::from);
    }

    @Transactional
    public void excluirVisitante(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado")
                );

        if (usuario.getRole() == Usuario.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Não é permitido excluir um administrador");
        }

        repository.delete(usuario);
    }

    public Usuario autenticar(String email, String senha) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas")
                );

        if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        if (usuario.getStatus() != Usuario.Status.ATIVO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário ainda não aprovado");
        }

        return usuario;
    }
}