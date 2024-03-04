package at.jku.dke.etutor.task_administration.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AuthJWKSourceTest {

    private Path privateKeyPath;
    private Path publicKeyPath;

    @BeforeEach
    void setUp() throws IOException {
        var dir = Files.createTempDirectory("jwk_test");
        this.privateKeyPath = dir.resolve("private_key.pem");
        this.publicKeyPath = dir.resolve("public_key.pem");
        this.createKeys();
    }

    @AfterEach
    void cleanUp() throws IOException {
        Files.walk(this.privateKeyPath.getParent())
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }

    @Test
    void getKeyId_notExistingKey_returnNull() {
        // Arrange
        var source = new AuthJWKSource("not_existing", "not_existing");

        // Act
        var result = source.getKeyId();

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void getKeyId_existingKey_returnId() {
        // Arrange
        var source = new AuthJWKSource(this.privateKeyPath.toString(), publicKeyPath.toString());

        // Act
        var result = source.getKeyId();

        // Assert
        assertThat(result).isNotNull().isNotEmpty().isNotEqualTo("0");
    }

    @Test
    void getPublicKey_existingKey_returnKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Arrange
        var lastModification = Files.getLastModifiedTime(this.publicKeyPath);
        var source = new AuthJWKSource(this.privateKeyPath.toString(), publicKeyPath.toString());

        // Act
        var result = source.getPublicKey();

        // Assert
        assertThat(result).isNotNull();
        assertThat(Files.getLastModifiedTime(this.publicKeyPath)).isEqualByComparingTo(lastModification);
    }

    @Test
    void getPrivateKey_existingKey_returnKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Arrange
        var lastModification = Files.getLastModifiedTime(this.privateKeyPath);
        var source = new AuthJWKSource(this.privateKeyPath.toString(), publicKeyPath.toString());

        // Act
        var result = source.getPrivateKey();

        // Assert
        assertNotNull(result);
        assertThat(Files.getLastModifiedTime(this.privateKeyPath)).isEqualByComparingTo(lastModification);
    }

    @Test
    void getPublicKey_onlyPrivateExists_generateNewKeysAndReturnNewPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Arrange
        Files.deleteIfExists(this.publicKeyPath);
        var source = new AuthJWKSource(this.privateKeyPath.toString(), publicKeyPath.toString());

        // Act
        var result = source.getPublicKey();

        // Assert
        assertThat(result).isNotNull();
        assertThat(this.privateKeyPath).exists();
        assertThat(this.publicKeyPath).exists();
    }

    @Test
    void getPublicKey_tooOldKey_generateNewKeysAndReturnNewPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Arrange
        var lastModification = Files.getLastModifiedTime(this.privateKeyPath);
        Files.setLastModifiedTime(this.privateKeyPath, FileTime.from(Instant.now().minus(180, ChronoUnit.DAYS)));
        var source = new AuthJWKSource(this.privateKeyPath.toString(), publicKeyPath.toString());

        // Act
        var result = source.getPublicKey();

        // Assert
        assertNotNull(result);
        assertThat(this.privateKeyPath).exists();
        assertThat(this.publicKeyPath).exists();
        assertThat(Files.getLastModifiedTime(this.privateKeyPath)).isGreaterThan(lastModification);
    }

    private void createKeys() throws IOException {
        Files.writeString(this.privateKeyPath, "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");
        Files.writeString(this.publicKeyPath, "MIIBpTANBgkqhkiG9w0BAQEFAAOCAZIAMIIBjQKCAYQAlBc4a3pU8WZDFTgMu4QVDxxPPTji20S9dExW53yVlUJB7HInVklGAsDFQ2FHkYo2uUAX0YzQYeaLe2504y2t5ZzgGvpKmqg5L8iNTUjxgamgo6zcQ2Xe8c6QlP94KoS0K4StCzIe4zKr6iAvvCtwJbtdubb92WxYZ99D2aoxBMCzqtvDAQJ4p7NLHMcKgN7l75y9gixHWNIvEjsZJSdrRJT3HJJKsnV9JiJ2ZfWW/z3l0DTZgvIpRcjGLraV9vYAX8SDN55IUwTim3CE4cnR8n55ftQfjE4owMAp4ZtjYudY8mdNEJ82S0seONCTprSipNi+sCg4TvntLPV/8eCMsnixODctTyhf7jyOha5EkuoT4jqw94l+NK4eyLgS2KxckFGzaRDv5uiOx1NJF2UI0djtDq0eFfRYNJuAaKKzIzn/U4RT+Ts5UhvVJwQ5FMUihQFHzHsv5zGgmp1nyXailqPObePaP/B7usBqqwUjxsw3EdH7zRr4YXvt+61Ir/ybMWaFAgMBAAE=");
    }
}
