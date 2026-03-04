package br.com.concurseiro.api.comentario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(
        @NotBlank @Size(max = 100) String autor,
        @NotBlank @Size(max = 5000) String texto
) {}
