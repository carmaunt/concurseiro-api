package br.com.concurseiro.api.catalogo.importacao.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class CatalogoTextoParser {

    private static final Pattern ASSUNTO_PATTERN = Pattern.compile("^\\s*\\d+[\\.)-]\\s+.+");
    private static final Pattern SUBASSUNTO_PATTERN = Pattern.compile("^\\s*[*•-]\\s+.+");

    public CatalogoTexto parse(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Texto do catálogo é obrigatório");
        }

        List<String> linhas = texto.lines()
                .map(String::trim)
                .filter(linha -> !linha.isBlank())
                .toList();

        String disciplina = null;
        List<AssuntoTexto> assuntos = new ArrayList<>();
        AssuntoTexto assuntoAtual = null;

        for (String linha : linhas) {
            if (deveIgnorar(linha)) {
                continue;
            }

            if (disciplina == null) {
                disciplina = limparMarcacao(linha);
                continue;
            }

            if (ehAssunto(linha)) {
                String nomeAssunto = limparAssunto(linha);
                assuntoAtual = new AssuntoTexto(nomeAssunto, new ArrayList<>());
                assuntos.add(assuntoAtual);
                continue;
            }

            if (ehSubassunto(linha)) {
                if (assuntoAtual == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Subassunto encontrado antes de qualquer assunto: " + linha
                    );
                }
                assuntoAtual.subassuntos().add(limparSubassunto(linha));
            }
        }

        if (disciplina == null || disciplina.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não foi possível identificar a disciplina");
        }

        if (assuntos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhum assunto foi identificado no texto");
        }

        return new CatalogoTexto(disciplina, assuntos);
    }

    private boolean deveIgnorar(String linha) {
        String normalizada = linha.toLowerCase();
        return normalizada.startsWith("perfeito,")
                || normalizada.contains("agora ficou claro")
                || normalizada.contains("assunto principal")
                || normalizada.contains("sem ramificar");
    }

    private boolean ehAssunto(String linha) {
        return ASSUNTO_PATTERN.matcher(linha).matches();
    }

    private boolean ehSubassunto(String linha) {
        return SUBASSUNTO_PATTERN.matcher(linha).matches();
    }

    private String limparAssunto(String linha) {
        return linha.replaceFirst("^\\s*\\d+[\\.)-]\\s+", "").trim();
    }

    private String limparSubassunto(String linha) {
        return linha.replaceFirst("^\\s*[*•-]\\s+", "").trim();
    }

    private String limparMarcacao(String linha) {
        return linha.replace("#", "").trim();
    }

    public record CatalogoTexto(String disciplina, List<AssuntoTexto> assuntos) {}

    public record AssuntoTexto(String nome, List<String> subassuntos) {}
}
