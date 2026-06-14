package br.com.concurseiro.api.questoes.enunciado.dto;

import br.com.concurseiro.api.questoes.enunciado.model.Enunciado;

import java.time.OffsetDateTime;

public record EnunciadoResponse(
        Long id,
        String conteudo,
        OffsetDateTime criadoEm
) {
    public static EnunciadoResponse fromEntity(Enunciado enunciado) {
        return new EnunciadoResponse(
                enunciado.getId(),
                enunciado.getConteudo(),
                enunciado.getCriadoEm()
        );
    }
}
