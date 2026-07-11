package br.com.concurseiro.api.conteudo.dto;

import br.com.concurseiro.api.conteudo.model.CategoriaEditorial;
import br.com.concurseiro.api.conteudo.model.StatusTaxonomia;
import java.time.OffsetDateTime;

public record CategoriaEditorialResponse(
        Long id,
        String nome,
        String slug,
        String descricao,
        StatusTaxonomia status,
        long quantidadeConteudos,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static CategoriaEditorialResponse fromEntity(CategoriaEditorial categoria, long quantidadeConteudos) {
        return new CategoriaEditorialResponse(categoria.getId(), categoria.getNome(), categoria.getSlug(),
                categoria.getDescricao(), categoria.getStatus(), quantidadeConteudos,
                categoria.getCreatedAt(), categoria.getUpdatedAt());
    }
}
