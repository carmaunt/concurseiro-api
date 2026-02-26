package br.com.concurseiro.api.usuarios.service;

import br.com.concurseiro.api.usuarios.model.Usuario;
import br.com.concurseiro.api.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
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