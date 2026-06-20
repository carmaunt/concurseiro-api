package br.com.concurseiro.api.usuarios.dto;

import br.com.concurseiro.api.usuarios.model.Usuario;

public record UsuarioPublicoResponse(
        Long id,
        String nome,
        String email,
        Usuario.Role role,
        Usuario.Status status,
        Usuario.TipoConta tipoConta
) {
    public static UsuarioPublicoResponse from(Usuario u) {
        return new UsuarioPublicoResponse(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getRole(),
                u.getStatus(),
                u.getTipoConta()
        );
    }
}
