package br.com.concurseiro.api.questoes.textoapoio.service;

import br.com.concurseiro.api.questoes.textoapoio.dto.TextoApoioRequest;
import br.com.concurseiro.api.questoes.textoapoio.dto.TextoApoioResponse;
import br.com.concurseiro.api.questoes.textoapoio.model.TextoApoio;
import br.com.concurseiro.api.questoes.textoapoio.repository.TextoApoioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class TextoApoioService {

    private static final int MAX_PAGE_SIZE = 50;

    private final TextoApoioRepository repository;

    public TextoApoioService(TextoApoioRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TextoApoioResponse cadastrar(TextoApoioRequest request) {
        if (request.conteudo() == null || request.conteudo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "conteúdo do texto de apoio é obrigatório");
        }

        TextoApoio textoApoio = obterOuCriar(request.titulo(), request.conteudo());
        return TextoApoioResponse.fromEntity(textoApoio);
    }

    @Transactional(readOnly = true)
    public TextoApoioResponse buscar(Long id) {
        TextoApoio textoApoio = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Texto de apoio não encontrado"));

        return TextoApoioResponse.fromEntity(textoApoio);
    }

    @Transactional(readOnly = true)
    public Page<TextoApoioResponse> listar(String titulo, int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page não pode ser negativa");
        }

        if (size < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size deve ser maior que zero");
        }

        if (size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size máximo permitido é " + MAX_PAGE_SIZE);
        }

        PageRequest pageable = PageRequest.of(page, size);

        if (titulo == null || titulo.isBlank()) {
            return repository.findAll(pageable).map(TextoApoioResponse::fromEntity);
        }

        return repository.findByTituloContainingIgnoreCase(titulo.trim(), pageable)
                .map(TextoApoioResponse::fromEntity);
    }

    @Transactional
    public TextoApoio resolverTextoApoio(Long textoApoioId, String titulo, String conteudo) {
        if (textoApoioId != null) {
            return repository.findById(textoApoioId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Texto de apoio não encontrado"));
        }

        if (conteudo == null || conteudo.isBlank()) {
            return null;
        }

        return obterOuCriar(titulo, conteudo);
    }

    private TextoApoio obterOuCriar(String titulo, String conteudo) {
        String conteudoNormalizado = normalizarConteudo(conteudo);
        String hash = gerarHashSha256(conteudoNormalizado);

        return repository.findByHashSha256(hash)
                .orElseGet(() -> salvarNovo(titulo, conteudoNormalizado, hash));
    }

    private TextoApoio salvarNovo(String titulo, String conteudo, String hash) {
        TextoApoio textoApoio = new TextoApoio();
        textoApoio.setTitulo(normalizarTitulo(titulo));
        textoApoio.setConteudo(conteudo);
        textoApoio.setHashSha256(hash);

        try {
            return repository.save(textoApoio);
        } catch (DataIntegrityViolationException ex) {
            return repository.findByHashSha256(hash)
                    .orElseThrow(() -> ex);
        }
    }

    private String normalizarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            return null;
        }

        return titulo.trim().replaceAll("\\s+", " ");
    }

    private String normalizarConteudo(String conteudo) {
        return conteudo
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .trim();
    }

    private String gerarHashSha256(String conteudo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(conteudo.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 não está disponível", ex);
        }
    }
}
