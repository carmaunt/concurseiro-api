package br.com.concurseiro.api.questoes.textoapoio.dto;

import jakarta.validation.constraints.Size;

public record TextoApoioRequest(
        @Size(max = 255) String titulo,
        @Size(max = 50000) String conteudo
) {}
