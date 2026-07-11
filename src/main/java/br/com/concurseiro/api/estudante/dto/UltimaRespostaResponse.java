package br.com.concurseiro.api.estudante.dto;

import br.com.concurseiro.api.questoes.resposta.model.RespostaQuestaoUsuario;
import java.time.OffsetDateTime;

public record UltimaRespostaResponse(
        String idQuestion,
        String disciplina,
        String respostaSelecionada,
        String gabarito,
        boolean acertou,
        OffsetDateTime respondidaEm
) {
    public static UltimaRespostaResponse fromEntity(RespostaQuestaoUsuario resposta) {
        return new UltimaRespostaResponse(
                resposta.getIdQuestion(),
                resposta.getDisciplina(),
                resposta.getRespostaSelecionada(),
                resposta.getGabarito(),
                resposta.isAcertou(),
                resposta.getRespondidaEm()
        );
    }
}
