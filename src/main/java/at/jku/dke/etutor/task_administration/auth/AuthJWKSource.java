package at.jku.dke.etutor.task_administration.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Objects;

/**
 * Provides the Json Web Key.
 * <p>
 * If no keys exist, a new key will be generated.
 * New keys will also be generated if the keys are older than 30 days.
 */
@Service
public class AuthJWKSource {

    private static final Logger LOG = LoggerFactory.getLogger(AuthJWKSource.class);

    private final Path privateKeyPath;
    private final Path publicKeyPath;
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;
    private String keyId;

    /**
     * Creates a new instance of class {@link AuthJWKSource}.
     *
     * @param privateKeyPath The path to the private key.
     * @param publicKeyPath  The path to the public key.
     */
    public AuthJWKSource(@Value("${jwt.private-key}") String privateKeyPath, @Value("${jwt.public-key}") String publicKeyPath) {
        Objects.requireNonNull(privateKeyPath);
        Objects.requireNonNull(publicKeyPath);

        this.privateKeyPath = Path.of(privateKeyPath);
        this.publicKeyPath = Path.of(publicKeyPath);
    }

    /**
     * Gets the key ID.
     *
     * @return The key ID.
     */
    public String getKeyId() {
        if (this.keyId == null && Files.exists(this.publicKeyPath)) {
            try {
                this.keyId = Files.readAttributes(this.publicKeyPath, BasicFileAttributes.class).lastModifiedTime().toMillis() + "";
            } catch (IOException e) {
                this.keyId = "0";
            }
        }
        return this.keyId;
    }

    /**
     * Gets the public key.
     *
     * @return The public key.
     * @throws NoSuchAlgorithmException If the RSA algorithm is not supported.
     * @throws InvalidKeySpecException  If the key specification is invalid.
     * @throws IOException              If an I/O error occurs.
     */
    public RSAPublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        if (this.publicKey == null)
            this.loadKeys();
        return this.publicKey;
    }

    /**
     * Gets the private key.
     *
     * @return The private key.
     * @throws NoSuchAlgorithmException If the RSA algorithm is not supported.
     * @throws InvalidKeySpecException  If the key specification is invalid.
     * @throws IOException              If an I/O error occurs.
     */
    public RSAPrivateKey getPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        if (this.privateKey == null)
            this.loadKeys();
        return this.privateKey;
    }

    /**
     * Generates a new key pair and saves it to the configured paths.
     *
     * @throws NoSuchAlgorithmException If the RSA algorithm is not supported.
     * @throws IOException              If an I/O error occurs.
     */
    public void generateKeys() throws NoSuchAlgorithmException, IOException {
        LOG.warn("JWK key files do not exist. Creating keys, please wait, this could take some time ...");

        // generate
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(3096);
        KeyPair pair = generator.generateKeyPair();
        this.publicKey = (RSAPublicKey) pair.getPublic();
        this.privateKey = (RSAPrivateKey) pair.getPrivate();

        // save
        Files.writeString(this.publicKeyPath, Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
        Files.writeString(this.privateKeyPath, Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
    }

    private void loadKeys() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        // if keys do not exist, generate them
        if (!Files.exists(this.publicKeyPath) || !Files.exists(this.privateKeyPath)) {
            this.generateKeys();
            return;
        }

        // if keys are too old, generate new ones
        if (Files.readAttributes(this.privateKeyPath, BasicFileAttributes.class).lastModifiedTime().toInstant().isBefore(Instant.now().minus(30, ChronoUnit.DAYS))) {
            this.generateKeys();
            return;
        }

        LOG.info("Loading JWT key");
        var keyFactory = KeyFactory.getInstance("RSA");

        // public
        var publicKeyString = Files.readString(this.publicKeyPath);
        var pkb = Base64.getDecoder().decode(publicKeyString);
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pkb);
        this.publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

        // private
        var privateKeyString = Files.readString(this.privateKeyPath);
        var prkb = Base64.getDecoder().decode(privateKeyString);
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(prkb);
        this.privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
    }
}
