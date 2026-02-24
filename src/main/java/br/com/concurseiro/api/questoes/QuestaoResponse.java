package br.com.concurseiro.api.questoes;

import java.time.OffsetDateTime;

public record QuestaoResponse(
        String idQuestion,
        String enunciado,
        String questao,
        String alternativas,

        String disciplina,
        Long disciplinaId,

        String assunto,
        Long assuntoId,

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
        Long disciplinaId = q.getDisciplinaCatalogo() != null ? q.getDisciplinaCatalogo().getId() : null;
        Long assuntoId = q.getAssuntoCatalogo() != null ? q.getAssuntoCatalogo().getId() : null;

        return new QuestaoResponse(
                q.getIdQuestion(),
                q.getEnunciado(),
                q.getQuestao(),
                q.getAlternativas(),

                q.getDisciplina(),
                disciplinaId,

                q.getAssunto(),
                assuntoId,

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