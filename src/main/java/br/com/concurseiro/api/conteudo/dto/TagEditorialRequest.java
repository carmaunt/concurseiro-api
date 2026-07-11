package br.com.concurseiro.api.conteudo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagEditorialRequest(
        @NotBlank @Size(max = 100) String nome,
        @Size(max = 120) String slug
) {}
