package br.com.concurseiro.api.questoes.dto;

import br.com.concurseiro.api.questoes.model.Questao;

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
        Long bancaId,

        String instituicao,
        Long instituicaoId,

        Integer ano,
        String cargo,
        String nivel,
        String modalidade,
        String gabarito,
        Long provaId,
        OffsetDateTime criadoEm
) {
    public static QuestaoResponse fromEntity(Questao q) {
        Long disciplinaId = q.getDisciplinaCatalogo() != null ? q.getDisciplinaCatalogo().getId() : null;
        Long assuntoId = q.getAssuntoCatalogo() != null ? q.getAssuntoCatalogo().getId() : null;
        Long bancaId = q.getBancaCatalogo() != null ? q.getBancaCatalogo().getId() : null;
        Long instituicaoId = q.getInstituicaoCatalogo() != null
                ? q.getInstituicaoCatalogo().getId()
                : null;

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
                bancaId,

                q.getInstituicao(),
                instituicaoId,

                q.getAno(),
                q.getCargo(),
                q.getNivel(),
                q.getModalidade(),
                q.getGabarito(),
                q.getProvaId(),
                q.getCriadoEm()
        );
    }
}