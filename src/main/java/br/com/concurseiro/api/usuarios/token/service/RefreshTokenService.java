package br.com.concurseiro.api.usuarios.token.service;

import br.com.concurseiro.api.usuarios.model.Usuario;
import br.com.concurseiro.api.usuarios.token.model.RefreshToken;
import br.com.concurseiro.api.usuarios.token.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.refresh-token-expiration:2592000}")
    private long refreshTokenExpirationSeconds;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken criar(Usuario usuario) {
        revogarTodosDoUsuario(usuario.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(gerarTokenSeguro());
        refreshToken.setUsuario(usuario);
        refreshToken.setRevogado(false);
        refreshToken.setExpiraEm(OffsetDateTime.now().plusSeconds(refreshTokenExpirationSeconds));

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> buscarValido(String token) {
        return refreshTokenRepository.buscarValidoComUsuario(token, OffsetDateTime.now());
    }

    @Transactional
    public void revogar(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevogado(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Transactional
    public void revogarTodosDoUsuario(Long usuarioId) {
        refreshTokenRepository.findAllByUsuarioIdAndRevogadoFalse(usuarioId)
                .forEach(rt -> rt.setRevogado(true));
    }

    private String gerarTokenSeguro() {
        byte[] buffer = new byte[64];
        SECURE_RANDOM.nextBytes(buffer);
        return BASE64_URL.encodeToString(buffer);
    }
}