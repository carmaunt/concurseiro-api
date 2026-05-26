package br.com.concurseiro.api.prova.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProvaQuestaoComTextoRequest(
        @Size(max = 10000) String enunciado,
        @NotBlank @Size(max = 10000) String questao,
        @Size(max = 10000) String alternativas,
        Long textoApoioId,
        @Size(max = 255) String textoApoioTitulo,
        @Size(max = 50000) String textoApoioConteudo,
        @NotNull Long disciplinaId,
        @NotNull Long assuntoId,
        String subassunto,
        @NotBlank String gabarito
) {}
