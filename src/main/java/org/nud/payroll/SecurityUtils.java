package org.nud.payroll;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A handy utility class for handling security stuff, like hashing passwords!
 */
public class SecurityUtils {

    /**
     * Hashes a password using SHA-256 so we don't store it in plain text.
     * 
     * @param password The plain text password to hash
     * @return The hashed password as a hex string
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // This should never happen since SHA-256 is built-in, but just in case!
            throw new RuntimeException("SHA-256 algorithm not found!", e);
        }
    }
}
