package br.com.concurseiro.api.prova.dto;

import jakarta.validation.constraints.*;

public record ProvaRequest(
        @NotBlank @Size(max = 160) String banca,
        @NotNull Long instituicaoId,
        @NotNull @Min(1900) @Max(2100) Integer ano,
        @NotBlank @Size(max = 160) String cargo,
        @NotBlank @Size(max = 80) String nivel,
        @NotBlank
        @Pattern(regexp = "^(A_E|A_D|CERTO_ERRADO|MULTIPLA ESCOLHA|MÚLTIPLA ESCOLHA|CERTO E ERRADO|CERTO/ERRADO)$",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "modalidade deve ser A_E, A_D, CERTO_ERRADO, MÚLTIPLA ESCOLHA ou CERTO E ERRADO")
        String modalidade
) {}
