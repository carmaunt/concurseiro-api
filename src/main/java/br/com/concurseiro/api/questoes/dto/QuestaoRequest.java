package br.com.concurseiro.api.questoes.dto;

import jakarta.validation.constraints.*;

public record QuestaoRequest(
        @Size(max = 10000) String enunciado,
        @NotBlank @Size(max = 10000) String questao,
        @NotBlank @Size(max = 10000) String alternativas,
        @Size(max = 50000) String explicacao,

        Long textoApoioId,
        @Size(max = 255) String textoApoioTitulo,
        @Size(max = 30) String textoApoioTipo,
        @Size(max = 50000) String textoApoioConteudo,
        @Size(max = 50000) String textoApoioJson,
        Long enunciadoId,

        // ===== Catálogo (agora obrigatório) =====
        @NotNull Long disciplinaId,
        @NotNull Long assuntoId,
        Long subassuntoId,
        @NotNull Long bancaId,
        @NotNull Long instituicaoId,

        @NotNull @Min(1900) @Max(2100) Integer ano,
        @NotBlank @Size(max = 160) String cargo,
        @NotBlank @Size(max = 80) String nivel,

        @NotBlank
        @Pattern(regexp = "^(A_E|A_D|CERTO_ERRADO)$",
                message = "modalidade deve ser A_E, A_D ou CERTO_ERRADO")
        String modalidade,

        @NotBlank
        String gabarito
) {
    public QuestaoRequest(
            String enunciado,
            String questao,
            String alternativas,
            Long disciplinaId,
            Long assuntoId,
            Long bancaId,
            Long instituicaoId,
            Integer ano,
            String cargo,
            String nivel,
            String modalidade,
            String gabarito
    ) {
        this(
                enunciado,
                questao,
                alternativas,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                disciplinaId,
                assuntoId,
                null,
                bancaId,
                instituicaoId,
                ano,
                cargo,
                nivel,
                modalidade,
                gabarito
        );
    }

    public QuestaoRequest(
            String enunciado,
            String questao,
            String alternativas,
            Long textoApoioId,
            String textoApoioTitulo,
            String textoApoioTipo,
            String textoApoioConteudo,
            String textoApoioJson,
            Long enunciadoId,
            Long disciplinaId,
            Long assuntoId,
            Long subassuntoId,
            Long bancaId,
            Long instituicaoId,
            Integer ano,
            String cargo,
            String nivel,
            String modalidade,
            String gabarito
    ) {
        this(
                enunciado, questao, alternativas, null,
                textoApoioId, textoApoioTitulo, textoApoioTipo, textoApoioConteudo, textoApoioJson,
                enunciadoId, disciplinaId, assuntoId, subassuntoId, bancaId, instituicaoId,
                ano, cargo, nivel, modalidade, gabarito
        );
    }

    public QuestaoRequest(
            String enunciado,
            String questao,
            String alternativas,
            Long textoApoioId,
            String textoApoioTitulo,
            String textoApoioTipo,
            String textoApoioConteudo,
            String textoApoioJson,
            Long disciplinaId,
            Long assuntoId,
            Long subassuntoId,
            Long bancaId,
            Long instituicaoId,
            Integer ano,
            String cargo,
            String nivel,
            String modalidade,
            String gabarito
    ) {
        this(
                enunciado,
                questao,
                alternativas,
                null,
                textoApoioId,
                textoApoioTitulo,
                textoApoioTipo,
                textoApoioConteudo,
                textoApoioJson,
                null,
                disciplinaId,
                assuntoId,
                subassuntoId,
                bancaId,
                instituicaoId,
                ano,
                cargo,
                nivel,
                modalidade,
                gabarito
        );
    }
}
