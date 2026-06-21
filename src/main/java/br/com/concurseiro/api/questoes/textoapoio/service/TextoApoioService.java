package br.com.concurseiro.api.questoes.textoapoio.service;

import br.com.concurseiro.api.questoes.textoapoio.dto.TextoApoioRequest;
import br.com.concurseiro.api.questoes.textoapoio.dto.TextoApoioResponse;
import br.com.concurseiro.api.questoes.textoapoio.model.TextoApoio;
import br.com.concurseiro.api.questoes.textoapoio.repository.TextoApoioRepository;
import org.springframework.dao.DataIntegrityViolationException;
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
import java.util.Locale;

@Service
public class TextoApoioService {

    private static final int MAX_PAGE_SIZE = 50;

    private final TextoApoioRepository repository;

    public TextoApoioService(TextoApoioRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TextoApoioResponse cadastrar(TextoApoioRequest request) {
        TextoApoio.Tipo tipo = normalizarTipo(request.tipo());
        String conteudo = normalizarConteudoOuNull(request.conteudo());
        String conteudoJson = normalizarConteudoOuNull(request.conteudoJson());

        validarConteudoObrigatorio(tipo, conteudo, conteudoJson);

        TextoApoio textoApoio = obterOuCriar(request.titulo(), tipo, conteudo, conteudoJson);
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

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "criadoEm"));

        if (titulo == null || titulo.isBlank()) {
            return repository.findAll(pageable).map(TextoApoioResponse::fromEntity);
        }

        return repository.findByTituloContainingIgnoreCase(titulo.trim(), pageable)
                .map(TextoApoioResponse::fromEntity);
    }

    @Transactional
    public TextoApoio resolverTextoApoio(Long textoApoioId, String titulo, String conteudo) {
        return resolverTextoApoio(textoApoioId, titulo, null, conteudo, null);
    }

    @Transactional
    public TextoApoio resolverTextoApoio(Long textoApoioId, String titulo, String tipo, String conteudo, String conteudoJson) {
        if (textoApoioId != null) {
            return repository.findById(textoApoioId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Texto de apoio não encontrado"));
        }

        TextoApoio.Tipo tipoNormalizado = normalizarTipo(tipo);
        String conteudoNormalizado = normalizarConteudoOuNull(conteudo);
        String conteudoJsonNormalizado = normalizarConteudoOuNull(conteudoJson);

        if (conteudoNormalizado == null && conteudoJsonNormalizado == null) {
            return null;
        }

        validarConteudoObrigatorio(tipoNormalizado, conteudoNormalizado, conteudoJsonNormalizado);

        return obterOuCriar(titulo, tipoNormalizado, conteudoNormalizado, conteudoJsonNormalizado);
    }

    private TextoApoio obterOuCriar(String titulo, TextoApoio.Tipo tipo, String conteudo, String conteudoJson) {
        String tituloNormalizado = normalizarTitulo(titulo);
        String hash = gerarHashSha256(montarBaseHash(tituloNormalizado, tipo, conteudo, conteudoJson));

        return repository.findByHashSha256(hash)
                .orElseGet(() -> salvarNovo(tituloNormalizado, tipo, conteudo, conteudoJson, hash));
    }

    private TextoApoio salvarNovo(String titulo, TextoApoio.Tipo tipo, String conteudo, String conteudoJson, String hash) {
        TextoApoio textoApoio = new TextoApoio();
        textoApoio.setTitulo(titulo);
        textoApoio.setTipo(tipo);
        textoApoio.setConteudo(conteudo == null ? "" : conteudo);
        textoApoio.setConteudoJson(conteudoJson);
        textoApoio.setHashSha256(hash);

        try {
            return repository.save(textoApoio);
        } catch (DataIntegrityViolationException ex) {
            return repository.findByHashSha256(hash)
                    .orElseThrow(() -> ex);
        }
    }

    private void validarConteudoObrigatorio(TextoApoio.Tipo tipo, String conteudo, String conteudoJson) {
        if (tipo == TextoApoio.Tipo.TABELA || tipo == TextoApoio.Tipo.IMAGEM) {
            if (conteudoJson == null || conteudoJson.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "conteúdo JSON do texto de apoio é obrigatório para " + tipo.name().toLowerCase(Locale.ROOT)
                );
            }
            return;
        }

        if (conteudo == null || conteudo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "conteúdo do texto de apoio é obrigatório");
        }
    }

    private TextoApoio.Tipo normalizarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return TextoApoio.Tipo.TEXTO;
        }

        try {
            return TextoApoio.Tipo.valueOf(tipo.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tipo de texto de apoio inválido");
        }
    }

    private String normalizarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            return null;
        }

        return titulo.trim().replaceAll("\\s+", " ");
    }

    private String normalizarConteudoOuNull(String conteudo) {
        if (conteudo == null || conteudo.isBlank()) {
            return null;
        }

        return conteudo
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .trim();
    }

    private String montarBaseHash(String titulo, TextoApoio.Tipo tipo, String conteudo, String conteudoJson) {
        return "titulo=" + valorHash(titulo) + "\n" +
                "tipo=" + (tipo == null ? TextoApoio.Tipo.TEXTO.name() : tipo.name()) + "\n" +
                "conteudo=" + valorHash(conteudo) + "\n" +
                "conteudoJson=" + valorHash(conteudoJson);
    }

    private String valorHash(String valor) {
        return valor == null ? "" : valor;
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
