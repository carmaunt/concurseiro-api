package br.com.concurseiro.api.questoes.dto;

public record RespostaQuestaoAmostraResponse(
        String idQuestion,
        String respostaSelecionada,
        String gabarito,
        boolean acertou,
        String explicacao
) {}
