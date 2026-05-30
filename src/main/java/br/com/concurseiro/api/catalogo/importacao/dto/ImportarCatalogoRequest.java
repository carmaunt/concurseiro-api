package br.com.concurseiro.api.catalogo.importacao.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ImportarCatalogoRequest(
        @NotBlank @Size(max = 160) String disciplina,
        @NotEmpty @Valid List<AssuntoImportacaoRequest> assuntos
) {

    public record AssuntoImportacaoRequest(
            @NotBlank @Size(max = 200) String nome,
            @Valid List<@NotBlank @Size(max = 200) String> subassuntos
    ) {}
}
