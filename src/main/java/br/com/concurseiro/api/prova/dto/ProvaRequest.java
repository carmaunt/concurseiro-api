package br.com.concurseiro.api.prova.dto;

import jakarta.validation.constraints.*;

public record ProvaRequest(
        @NotBlank @Size(max = 160) String banca,
        @NotNull Long instituicaoId,
        @NotNull @Min(1900) @Max(2100) Integer ano,
        @NotBlank @Size(max = 160) String cargo,
        @NotBlank @Size(max = 80) String nivel,
        @NotBlank
        @Pattern(regexp = "^(A_E|A_D|CERTO_ERRADO|MULTIPLA ESCOLHA|MÚLTIPLA ESCOLHA|MULTIPLA ESCOLHA A[_-][DE]|MÚLTIPLA ESCOLHA A[_-][DE]|CERTO E ERRADO|CERTO/ERRADO)$",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "modalidade deve ser MÚLTIPLA ESCOLHA A-E, MÚLTIPLA ESCOLHA A-D, CERTO E ERRADO, A_E, A_D ou CERTO_ERRADO")
        String modalidade
) {}
