package br.com.concurseiro.api.questoes.textoapoio.dto;

import br.com.concurseiro.api.questoes.textoapoio.model.TextoApoio;

import java.time.OffsetDateTime;

public record TextoApoioResponse(
        Long id,
        String titulo,
        String tipo,
        String conteudo,
        String conteudoJson,
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
                textoApoio.getTipo() == null ? "TEXTO" : textoApoio.getTipo().name(),
                textoApoio.getConteudo(),
                textoApoio.getConteudoJson(),
                textoApoio.getHashSha256(),
                textoApoio.getCriadoEm()
        );
    }
}