package at.jku.dke.etutor.task_administration.services;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Provides a method to generate random values.
 */
public final class RandomService {

    /**
     * The one and only instance of this class.
     */
    public static final RandomService INSTANCE = new RandomService();
    private static final String ALLOWED_CHARS = "0123456789abcdefghijklmnupqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_";

    private SecureRandom random;

    private RandomService() {
        try {
            this.random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException ex) {
            this.random = new SecureRandom();
        }
    }

    /**
     * Returns a pseudorandom generated String with the specified length.
     *
     * @param length The length of the requested random string.
     * @return Random string with the specified length
     */
    public String randomString(int length) {
        return this.random.ints(length, 0, ALLOWED_CHARS.length())
            .mapToObj(ALLOWED_CHARS::charAt)
            .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
            .toString();
    }
}
