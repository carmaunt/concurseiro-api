package br.com.concurseiro.api.infra.security;

import br.com.concurseiro.api.usuarios.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "a-test-secret-key-that-is-long-enough-32chars";
    private static final long EXPIRATION_MS = 3600000L;

    private JwtService jwtService;

    @BeforeEach
    void setup() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);
    }

    @Test
    void constructor_deveFalhar_quandoSecretVazio() {
        assertThrows(IllegalStateException.class, () -> new JwtService("", 3600000L));
    }

    @Test
    void constructor_deveFalhar_quandoSecretCurto() {
        assertThrows(IllegalStateException.class, () -> new JwtService("short", 3600000L));
    }

    @Test
    void generateToken_deveRetornarTokenValido() {
        String token = jwtService.generateToken("user@test.com", Usuario.Role.VISITANTE);
        assertNotNull(token);
        assertEquals("user@test.com", jwtService.extractEmail(token));
    }

    @Test
    void extractRole_deveRetornarRoleCorreta() {
        String token = jwtService.generateToken("admin@test.com", Usuario.Role.ADMIN);
        assertEquals("ADMIN", jwtService.extractRole(token));
    }

    @Test
    void isValid_deveRetornarTrue_quandoTokenValido() {
        String token = jwtService.generateToken("user@test.com", Usuario.Role.VISITANTE);
        assertTrue(jwtService.isValid(token, "user@test.com"));
    }

    @Test
    void isValid_deveRetornarFalse_quandoEmailDiferente() {
        String token = jwtService.generateToken("user@test.com", Usuario.Role.VISITANTE);
        assertFalse(jwtService.isValid(token, "other@test.com"));
    }

    @Test
    void isValid_deveRetornarFalse_quandoTokenExpirado() throws InterruptedException {
        JwtService shortLived = new JwtService(SECRET, 1L);
        String token = shortLived.generateToken("user@test.com", Usuario.Role.VISITANTE);
        Thread.sleep(50);
        try {
            boolean result = shortLived.isValid(token, "user@test.com");
            assertFalse(result);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            assertTrue(true);
        }
    }
}
