package br.com.concurseiro.api.conteudo.dto;

import br.com.concurseiro.api.conteudo.model.ConteudoPortal;
import java.time.OffsetDateTime;
import java.util.List;

public record ConteudoPortalResponse(
        Long id,
        String titulo,
        String slug,
        String resumo,
        String conteudo,
        String imagemCapa,
        String categoria,
        TaxonomiaResumoResponse category,
        List<TaxonomiaResumoResponse> tags,
        ConteudoPortal.Status status,
        ConteudoPortal.Tipo tipo,
        boolean destaque,
        OffsetDateTime publicadoEm,
        String seoTitulo,
        String seoDescricao,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ConteudoPortalResponse fromEntity(ConteudoPortal conteudo) {
        return new ConteudoPortalResponse(
                conteudo.getId(),
                conteudo.getTitulo(),
                conteudo.getSlug(),
                conteudo.getResumo(),
                conteudo.getConteudo(),
                conteudo.getImagemCapa(),
                conteudo.getCategoria() != null ? conteudo.getCategoria().getNome() : conteudo.getCategoriaLegada(),
                conteudo.getCategoria() == null ? null : new TaxonomiaResumoResponse(
                        conteudo.getCategoria().getId(), conteudo.getCategoria().getNome(), conteudo.getCategoria().getSlug()),
                conteudo.getTags().stream()
                        .map(tag -> new TaxonomiaResumoResponse(tag.getId(), tag.getNome(), tag.getSlug()))
                        .toList(),
                conteudo.getStatus(),
                conteudo.getTipo(),
                conteudo.isDestaque(),
                conteudo.getPublicadoEm(),
                conteudo.getSeoTitulo(),
                conteudo.getSeoDescricao(),
                conteudo.getCreatedAt(),
                conteudo.getUpdatedAt()
        );
    }
}
