package at.jku.dke.etutor.task_administration.config;

import at.jku.dke.etutor.task_administration.auth.AuthJWKSource;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

//    @Test
//    void defaultSecurityFilterChain() throws Exception {
//    }

    @Test
    void authenticationManager() {
        // Arrange
        var config = new SecurityConfig();
        var uds = mock(UserDetailsService.class);
        var pe = mock(PasswordEncoder.class);

        // Act
        var result = config.authenticationManager(uds, pe);

        // Assert
        assertNotNull(result);
    }

    @Test
    void passwordEncoder() {
        // Arrange
        var config = new SecurityConfig();

        // Act
        var result = config.passwordEncoder();

        // Assert
        assertNotNull(result);
    }

    @Test
    void jwtDecoder() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        // Arrange
        var config = new SecurityConfig();
        var source = new AuthJWKSource("test-private.pem", "test-public.pem");

        // Act
        var decoder = config.jwtDecoder(source);

        // Assert
        assertNotNull(decoder);
    }

    @Test
    void jwtEncoder() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        // Arrange
        var config = new SecurityConfig();
        var source = new AuthJWKSource("test-private.pem", "test-public.pem");

        // Act
        var encoder = config.jwtEncoder(source);

        // Assert
        assertNotNull(encoder);
    }

    @Test
    void customJwtAuthenticationConverter() {
        // Arrange
        var config = new SecurityConfig();

        // Act
        var result = config.customJwtAuthenticationConverter();

        // Assert
        assertNotNull(result);
    }
}
