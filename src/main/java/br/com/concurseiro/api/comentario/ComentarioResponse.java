package br.com.concurseiro.api.comentario;

import java.time.OffsetDateTime;

public record ComentarioResponse(
        Long id,
        String questaoId,
        String autor,
        String texto,
        Integer curtidas,
        Integer descurtidas,
        OffsetDateTime criadoEm
) {
    public static ComentarioResponse fromEntity(Comentario c) {
        return new ComentarioResponse(
                c.getId(),
                c.getQuestaoId(),
                c.getAutor(),
                c.getTexto(),
                c.getCurtidas(),
                c.getDescurtidas(),
                c.getCriadoEm()
        );
    }
}