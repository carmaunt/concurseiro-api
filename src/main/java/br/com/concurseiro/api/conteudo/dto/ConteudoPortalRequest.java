package br.com.concurseiro.api.conteudo.dto;

import br.com.concurseiro.api.conteudo.model.ConteudoPortal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record ConteudoPortalRequest(
        @NotBlank @Size(max = 180) String titulo,
        @Size(max = 220) String slug,
        @NotBlank @Size(max = 500) String resumo,
        @NotBlank String conteudo,
        @Size(max = 1000) String imagemCapa,
        Long categoriaId,
        Set<Long> tagIds,
        @NotNull ConteudoPortal.Status status,
        @NotNull ConteudoPortal.Tipo tipo,
        boolean destaque,
        @Size(max = 180) String seoTitulo,
        @Size(max = 300) String seoDescricao
) {}
