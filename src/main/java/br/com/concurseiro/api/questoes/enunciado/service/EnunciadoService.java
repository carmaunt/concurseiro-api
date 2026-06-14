package br.com.concurseiro.api.questoes.enunciado.service;

import br.com.concurseiro.api.questoes.enunciado.dto.EnunciadoResponse;
import br.com.concurseiro.api.questoes.enunciado.model.Enunciado;
import br.com.concurseiro.api.questoes.enunciado.repository.EnunciadoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class EnunciadoService {

    private static final int MAX_PAGE_SIZE = 200;

    private final EnunciadoRepository repository;

    public EnunciadoService(EnunciadoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Enunciado resolverEnunciado(Long enunciadoId, String conteudo) {
        if (enunciadoId != null) {
            return repository.findById(enunciadoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enunciado não encontrado"));
        }

        String conteudoNormalizado = normalizarConteudo(conteudo);
        String hash = gerarHashSha256(conteudoNormalizado);

        return repository.findByHashSha256(hash)
                .orElseGet(() -> salvarNovo(conteudoNormalizado, hash));
    }

    @Transactional(readOnly = true)
    public EnunciadoResponse buscar(Long id) {
        return repository.findById(id)
                .map(EnunciadoResponse::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enunciado não encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<EnunciadoResponse> listar(String texto, int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page não pode ser negativa");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "size deve estar entre 1 e " + MAX_PAGE_SIZE
            );
        }

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "conteudo"));
        Page<Enunciado> resultado = texto == null || texto.isBlank()
                ? repository.findAll(pageable)
                : repository.findByConteudoContainingIgnoreCase(texto.trim(), pageable);

        return resultado.map(EnunciadoResponse::fromEntity);
    }

    private Enunciado salvarNovo(String conteudo, String hash) {
        repository.inserirSeAusente(conteudo, hash);
        return repository.findByHashSha256(hash)
                .orElseThrow(() -> new IllegalStateException("Falha ao recuperar enunciado persistido"));
    }

    private String normalizarConteudo(String conteudo) {
        return (conteudo == null ? "" : conteudo)
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .trim();
    }

    private String gerarHashSha256(String conteudo) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(conteudo.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponível", ex);
        }
    }
}
