package br.com.concurseiro.api.comentario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(
        @NotBlank(message = "O nome do autor é obrigatório")
        @Size(max = 100, message = "O nome do autor deve ter no máximo 100 caracteres")
        String autor,

        @NotBlank(message = "O texto do comentário é obrigatório")
        @Size(max = 5000, message = "O comentário deve ter no máximo 5000 caracteres")
        String texto
) {}