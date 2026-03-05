package br.com.concurseiro.api.prova.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProvaQuestaoRequest(
        @NotBlank @Size(max = 10000) String enunciado,
        @NotBlank @Size(max = 10000) String questao,
        @Size(max = 10000) String alternativas,

        @NotBlank @Size(max = 160) String disciplina,
        @NotBlank @Size(max = 200) String assunto,

        Long disciplinaId,
        Long assuntoId,

        String subassunto,

        @NotBlank String gabarito
) {}
