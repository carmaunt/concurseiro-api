package br.com.concurseiro.api.questoes.textoapoio.dto;

import br.com.concurseiro.api.questoes.textoapoio.model.TextoApoio;

import java.time.OffsetDateTime;

public record TextoApoioResponse(
        Long id,
        String titulo,
        String conteudo,
        String hashSha256,
        OffsetDateTime criadoEm
) {
    public static TextoApoioResponse fromEntity(TextoApoio textoApoio) {
        if (textoApoio == null) {
            return null;
        }

        return new TextoApoioResponse(
                textoApoio.getId(),
                textoApoio.getTitulo(),
                textoApoio.getConteudo(),
                textoApoio.getHashSha256(),
                textoApoio.getCriadoEm()
        );
    }
}
