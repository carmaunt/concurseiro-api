package br.com.concurseiro.api.usuarios.token.repository;

import br.com.concurseiro.api.usuarios.token.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUsuarioIdAndRevogadoFalse(Long usuarioId);

    @Query("""
        select rt
        from RefreshToken rt
        join fetch rt.usuario u
        where rt.token = :token
          and rt.revogado = false
          and rt.expiraEm > :agora
    """)
    Optional<RefreshToken> buscarValidoComUsuario(String token, OffsetDateTime agora);
}