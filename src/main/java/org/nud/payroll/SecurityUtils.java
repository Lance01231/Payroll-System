package org.nud.payroll;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Password hashing and verification. New passwords use BCrypt; legacy SHA-256 hex hashes remain
 * verifiable and are upgraded to BCrypt on successful login.
 */
public final class SecurityUtils {

    private static final SecureRandom RNG = new SecureRandom();

    private SecurityUtils() {}

    /** Cost factor 12 — adjustable as hardware improves. */
    public static String hashPassword(String password) {
        String salt = BCrypt.gensalt(12, RNG);
        return BCrypt.hashpw(password, salt);
    }

    /**
     * Returns true if the plaintext matches the stored hash (BCrypt or legacy SHA-256 hex).
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        if (isBcryptHash(storedHash)) {
            try {
                return BCrypt.checkpw(plainPassword, storedHash);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        byte[] expected = storedHash.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII);
        byte[] actual = sha256HexBytes(plainPassword);
        return MessageDigest.isEqual(expected, actual);
    }

    /** True if this stored hash should be replaced with BCrypt after login. */
    public static boolean needsPasswordHashUpgrade(String storedHash) {
        return storedHash != null && !isBcryptHash(storedHash);
    }

    private static boolean isBcryptHash(String s) {
        return s.length() >= 4
                && s.charAt(0) == '$'
                && (s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$"));
    }

    private static byte[] sha256HexBytes(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            String hex = toHex(hash);
            return hex.getBytes(StandardCharsets.US_ASCII);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
