package br.com.concurseiro.api.questoes.dto;

import br.com.concurseiro.api.questoes.model.Questao;
import java.time.OffsetDateTime;

public record QuestaoResponse(
        String idQuestion,
        String enunciado,
        Long enunciadoId,
        String questao,
        String alternativas,
        Long textoApoioId,
        String textoApoioTitulo,
        String textoApoioConteudo,
        String textoApoioTipo,
        String textoApoioJson,
        String disciplina,
        Long disciplinaId,
        String assunto,
        Long assuntoId,
        String subassunto,
        Long subassuntoId,
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
        Long subassuntoId = q.getSubAssuntoCatalogo() != null ? q.getSubAssuntoCatalogo().getId() : null;
        Long bancaId = q.getBancaCatalogo() != null ? q.getBancaCatalogo().getId() : null;
        Long instituicaoId = q.getInstituicaoCatalogo() != null ? q.getInstituicaoCatalogo().getId() : null;

        Long textoApoioId = q.getTextoApoio() != null ? q.getTextoApoio().getId() : null;
        String textoApoioTitulo = q.getTextoApoio() != null ? q.getTextoApoio().getTitulo() : null;
        String textoApoioConteudo = q.getTextoApoio() != null ? q.getTextoApoio().getConteudo() : null;
        String textoApoioTipo = q.getTextoApoio() != null && q.getTextoApoio().getTipo() != null
                ? q.getTextoApoio().getTipo().name()
                : null;
        String textoApoioJson = q.getTextoApoio() != null ? q.getTextoApoio().getConteudoJson() : null;

        return new QuestaoResponse(
                q.getIdQuestion(),
                q.getEnunciado(),
                q.getEnunciadoCatalogo() != null ? q.getEnunciadoCatalogo().getId() : null,
                q.getQuestao(),
                q.getAlternativas(),
                textoApoioId,
                textoApoioTitulo,
                textoApoioConteudo,
                textoApoioTipo,
                textoApoioJson,
                q.getDisciplina(),
                disciplinaId,
                q.getAssunto(),
                assuntoId,
                q.getSubAssunto(),
                subassuntoId,
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
