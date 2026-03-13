package br.com.concurseiro.api.questoes.dto;

import jakarta.validation.constraints.*;

public record QuestaoRequest(
        @NotBlank @Size(max = 10000) String enunciado,
        @NotBlank @Size(max = 10000) String questao,
        @NotBlank @Size(max = 10000) String alternativas,

        // ===== Catálogo (agora obrigatório) =====
        @NotNull Long disciplinaId,
        @NotNull Long assuntoId,
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
) {}