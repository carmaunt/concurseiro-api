package br.com.concurseiro.api.questoes.resposta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RespostaQuestaoRequest(
        @NotBlank @Size(max = 20) String respostaSelecionada
) {}
