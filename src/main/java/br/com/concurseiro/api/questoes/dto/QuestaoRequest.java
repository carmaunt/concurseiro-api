package br.com.concurseiro.api.questoes.dto;

import jakarta.validation.constraints.*;

public record QuestaoRequest(
        @NotBlank @Size(max = 10000) String enunciado,
        @NotBlank @Size(max = 10000) String questao,
        @NotBlank @Size(max = 10000) String alternativas,

        // ===== Texto (mantém compatibilidade) =====
        @NotBlank @Size(max = 160) String disciplina,
        @NotBlank @Size(max = 200) String assunto,
        @NotBlank @Size(max = 160) String banca,
        @NotBlank @Size(max = 200) String instituicao,

        // ===== Catálogo (migração gradual) =====
        Long disciplinaId,
        Long assuntoId,
        Long bancaId,
        Long instituicaoId,

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