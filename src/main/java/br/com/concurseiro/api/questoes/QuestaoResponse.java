package br.com.concurseiro.api.questoes;

import java.time.OffsetDateTime;

public record QuestaoResponse(
        String idQuestion,
        String enunciado,
        String questao,
        String alternativas,
        String disciplina,
        String assunto,
        String banca,
        String instituicao,
        Integer ano,
        String cargo,
        String nivel,
        String modalidade,
        String gabarito,
        OffsetDateTime criadoEm
) {
    public static QuestaoResponse fromEntity(Questao q) {
        return new QuestaoResponse(
                q.getIdQuestion(),
                q.getEnunciado(),
                q.getQuestao(),
                q.getAlternativas(),
                q.getDisciplina(),
                q.getAssunto(),
                q.getBanca(),
                q.getInstituicao(),
                q.getAno(),
                q.getCargo(),
                q.getNivel(),
                q.getModalidade(),
                q.getGabarito(),
                q.getCriadoEm()
        );
    }
}