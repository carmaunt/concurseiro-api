package br.com.concurseiro.api.questoes.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

public final class QuestaoValidationHelper {

    private QuestaoValidationHelper() {}

    public static String gerarIdQuestion() {
        String hex = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return "Q" + hex.substring(0, 15);
    }

    public static String normalizarModalidade(String modalidadeBruta, String alternativas) {
        String m = modalidadeBruta.trim().toUpperCase();

        if (m.equals("MÚLTIPLA ESCOLHA") || m.equals("MULTIPLA ESCOLHA")) {
            return alternativas != null && alternativas.toUpperCase().contains("E)")
                    ? "A_E"
                    : "A_D";
        }

        if (m.equals("CERTO E ERRADO") || m.equals("CERTO/ERRADO")) {
            return "CERTO_ERRADO";
        }

        if (m.equals("A_E") || m.equals("A_D") || m.equals("CERTO_ERRADO")) {
            return m;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Modalidade inválida: " + modalidadeBruta);
    }

    public static void validarGabaritoPorModalidade(String modalidade, String gabarito) {
        boolean ok = switch (modalidade) {
            case "A_E" -> gabarito.matches("^[A-E]$");
            case "A_D" -> gabarito.matches("^[A-D]$");
            case "CERTO_ERRADO" -> gabarito.equals("C") || gabarito.equals("E")
                    || gabarito.equals("CERTO") || gabarito.equals("ERRADO");
            default -> false;
        };

        if (!ok) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Modalidade inválida ou gabarito incompatível (modalidade=" + modalidade + ")"
            );
        }
    }

    public static String normalizarGabarito(String modalidade, String gabarito) {
        if (!"CERTO_ERRADO".equals(modalidade)) return gabarito;

        return switch (gabarito) {
            case "CERTO" -> "C";
            case "ERRADO" -> "E";
            default -> gabarito;
        };
    }
}
