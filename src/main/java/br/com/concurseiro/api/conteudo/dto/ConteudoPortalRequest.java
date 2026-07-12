package br.com.concurseiro.api.conteudo.dto;

import br.com.concurseiro.api.conteudo.model.ConteudoPortal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.List;

public record ConteudoPortalRequest(
        @NotBlank @Size(max = 180) String titulo,
        @Size(max = 220) String slug,
        @NotBlank @Size(max = 500) String resumo,
        @NotBlank String conteudo,
        @Size(max = 1000) String imagemCapa,
        @Size(max = 500) String imagemCapaAlt,
        @Size(max = 160) String autorNome,
        @Size(max = 160) String revisadoPor,
        List<@jakarta.validation.Valid FonteEditorial> fontesOficiais,
        Long categoriaId,
        Set<Long> tagIds,
        @NotNull ConteudoPortal.Status status,
        @NotNull ConteudoPortal.Tipo tipo,
        boolean destaque,
        @Size(max = 180) String seoTitulo,
        @Size(max = 300) String seoDescricao
) {}
