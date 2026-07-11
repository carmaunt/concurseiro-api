package br.com.concurseiro.api.questoes.resposta.dto;

import br.com.concurseiro.api.questoes.resposta.model.RespostaQuestaoUsuario;
import java.time.OffsetDateTime;

public record RespostaQuestaoResponse(
        Long id,
        String idQuestion,
        String disciplina,
        String respostaSelecionada,
        String gabarito,
        boolean acertou,
        String explicacao,
        OffsetDateTime respondidaEm
) {
    public static RespostaQuestaoResponse fromEntity(RespostaQuestaoUsuario resposta, String explicacao) {
        return new RespostaQuestaoResponse(
                resposta.getId(),
                resposta.getIdQuestion(),
                resposta.getDisciplina(),
                resposta.getRespostaSelecionada(),
                resposta.getGabarito(),
                resposta.isAcertou(),
                explicacao,
                resposta.getRespondidaEm()
        );
    }
}
