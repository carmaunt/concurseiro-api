package br.com.concurseiro.api.conteudo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaEditorialRequest(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 140) String slug,
        @Size(max = 500) String descricao
) {}
