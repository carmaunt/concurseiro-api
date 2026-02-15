package br.com.concurseiro.api.usuarios;

public record UsuarioPublicoResponse(
        Long id,
        String nome,
        String email,
        Usuario.Role role,
        Usuario.Status status
) {
    public static UsuarioPublicoResponse from(Usuario u) {
        return new UsuarioPublicoResponse(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getRole(),
                u.getStatus()
        );
    }
}