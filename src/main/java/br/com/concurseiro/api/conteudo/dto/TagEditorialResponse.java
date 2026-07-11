package br.com.concurseiro.api.conteudo.dto;

import br.com.concurseiro.api.conteudo.model.StatusTaxonomia;
import br.com.concurseiro.api.conteudo.model.TagEditorial;
import java.time.OffsetDateTime;

public record TagEditorialResponse(
        Long id,
        String nome,
        String slug,
        StatusTaxonomia status,
        long quantidadeConteudos,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static TagEditorialResponse fromEntity(TagEditorial tag, long quantidadeConteudos) {
        return new TagEditorialResponse(tag.getId(), tag.getNome(), tag.getSlug(), tag.getStatus(),
                quantidadeConteudos, tag.getCreatedAt(), tag.getUpdatedAt());
    }
}
