package br.com.concurseiro.api.conteudo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FonteEditorial(
        @NotBlank @Size(max = 160) String nome,
        @NotBlank @Size(max = 1000) @Pattern(regexp = "https?://.+") String url
) {}
